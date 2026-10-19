package draco.format.dracon

import draco._
import draco.drake.{Absent, Presence, Present}
import draco.scalatarget.CirceCodecs
import io.circe.Json
import io.circe.syntax._
import org.scalatest.funsuite.AnyFunSuite

class DraconTest extends AnyFunSuite {
  private def contains[T](result: Presence[T], expected: T): Boolean = result.fold(false, _ == expected)
  private def missing[T](result: Presence[T]): Boolean = result.fold(true, _ => false)
  private def intDecoder = CirceCodecs.decoder(io.circe.Decoder.decodeInt)
  private def document = Dracon(Json.obj(
    "items" -> Json.arr(Json.obj("amount" -> Json.fromInt(7))),
    "nothing" -> Json.Null,
    "0" -> Json.fromString("object key")))

  test("explicit Encoder and Decoder capabilities work without target codec instances at the call site") {
    val encode = Encoder[Int](n => Dracon(Json.fromInt(n)))
    val decode = Decoder[Int](source => intDecoder.decode(source))
    assert(contains(decode.decode(encode.encode(42)), 42))
    assert(missing(decode.decode(Dracon(Json.fromString("wrong type")))))
  }

  test("Circe adapters bind encoding and decoding to the neutral signatures and retain JSON shapes") {
    val encoder = CirceCodecs.encoder(io.circe.Encoder.encodeList[Int](io.circe.Encoder.encodeInt))
    val decoder = CirceCodecs.decoder(io.circe.Decoder.decodeList[Int](io.circe.Decoder.decodeInt))
    val encoded = encoder.encode(List(1, 2, 3))
    assert(encoded.json == Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3)))
    assert(contains(decoder.decode(encoded), List(1, 2, 3)))
    assert(missing(decoder.decode(Dracon(Json.arr(Json.fromString("wrong type"))))))
  }

  test("selection returns Presence for object fields, arrays, missing paths, invalid indices and JSON null") {
    val source = document
    assert(source.select(Seq.empty).fold(false, _.json == source.json))
    assert(source.select(Seq("items", "0", "amount")).fold(false, _.json == Json.fromInt(7)))
    assert(source.select(Seq("0")).fold(false, _.json == Json.fromString("object key")))
    assert(source.select(Seq("nothing")).fold(false, _.json.isNull), "JSON null is a present value")
    Seq(Seq("missing"), Seq("items", "x"), Seq("items", "-1"), Seq("items", "999999999999999999999999"),
      Seq("items", "2"), Seq("items", "0", "amount", "field")).foreach(path => assert(missing(source.select(path)), path.toString))
  }

  test("Value selects before decoding and does not evaluate the capability for a missing path") {
    var calls = 0
    val decoder = Decoder[Int](source => { calls += 1; intDecoder.decode(source) })
    assert(contains(Value("amount", Seq("items", "0", "amount")).decode(document, decoder), 7))
    assert(calls == 1)
    assert(missing(Value("missing", Seq("missing")).decode(document, decoder)))
    assert(calls == 1)
    assert(missing(Value("null", Seq("nothing")).decode(document, decoder)))
    assert(calls == 2)
    assert(missing(Value("missing", Seq("missing")).decode[Int](document, throw new AssertionError("unused decoder evaluated"))))
  }

  test("Dracon and the path selector retain canonical JSON wire codecs") {
    val source = document
    assert(source.asJson.as[Dracon].toOption.get.json == source.json)
    assert(source.asJson.hcursor.downField("json").focus.contains(source.json))
    val selector = Value("amount", Seq("items", "0", "amount"))
    val decoded = selector.asJson.as[Value].toOption.get
    assert(decoded.name == selector.name && decoded.pathElements == selector.pathElements)
    assert(contains(decoded.decode(source, intDecoder), 7))
  }

  test("generic Format.Value selects without depending on Dracon or any target decoder") {
    val selector = new draco.format.Value[String] {
      override lazy val typeDefinition: TypeDefinition = draco.format.Value.typeDefinition
      override val name = "first"
      override val pathElements = Seq.empty[String]
      override def value(source: => String): Presence[String] = Present(source.take(1))
    }
    assert(contains(selector.value("abc"), "a"))
    val text = scala.io.Source.fromFile("src/main/resources/draco/format/Value.drake")
    try {
      val definition = text.mkString
      assert(!definition.contains("Decoder") && !definition.contains("dracon") && !definition.contains("io.circe"))
    } finally text.close()
  }

  test("Dracon replaces the JSON format domain and owns both capabilities and Value") {
    val domain = DomainBuilder.define("Dracon", Seq("draco", "format", "dracon"))
    assert(domain.typeDefinition.typeName == Dracon.typeDefinition.typeName)
    assert(domain.typeDictionary.elementTypes.map(_.typeName.name).toSet == Set("Encoder", "Decoder", "Value"))
    assert(DomainBuilder.validate(domain).isEmpty)
    assert(!java.nio.file.Files.exists(java.nio.file.Paths.get("src/main/resources/draco/format/json/JSON.json")))
    assert(TypeLoader.tryLoad(TypeName("JSON", Seq("draco", "format", "json"))).fold(true, _ => false))
    assert(draco.scalatarget.ScalaTarget.elementTypeNames == Seq("CirceCodecs"))
  }

  test("definition tree payload codecs preserve nested JSON without wrapper envelopes") {
    val wire = io.circe.parser.parse("""{"kind":"Fixed","name":"x","valueType":{"[]":["Text"]},"value":{"call":["f",null,{"json":42}]}}""").toOption.get
    val value = wire.as[draco.TypeElement].toOption.get
    assert(value.value.json == wire.hcursor.downField("value").focus.get)
    assert(value.asJson == wire)
    assert(draco.scalatarget.CirceCodecs.encodePayload(value.value) == value.value.json)
    assert(draco.scalatarget.CirceCodecs.decodePayload.decodeJson(value.value.json).toOption.contains(value.value))
    assert(value.value.asJson == Json.obj("json" -> value.value.json))
  }

  test("wrapped type parameters keep structural identity and bare parameter wire values") {
    val argument = Json.obj("[]" -> Json.arr(Json.fromString("Text")))
    val first = TypeName("F", Seq("example"), Seq(Dracon(argument)))
    val second = TypeName("F", Seq("example"), Seq(Dracon(argument)))
    assert(first == second)
    assert(first.hashCode == second.hashCode)
    assert(Set(first, second).size == 1)
    assert(first.asJson.hcursor.downField("typeParameters").focus.contains(Json.arr(argument)))
    assert(first.asJson.as[TypeName].toOption.contains(first))
    assert(TypeName.write(first).json == first.asJson)
    assert(TypeName.read(TypeName.write(first)).fold(false, _ == first))
  }

  test("neutral parent inspection distinguishes named and foreign derivations") {
    val named = TypeName("Parent", Seq("example"), Seq(Dracon.string("T")))
    val foreign = Dracon.record(Seq("{}" -> Dracon.array(Seq(Dracon.string("K"), Dracon.string("V")))))
    val aspect = draco.DracoAspect(_derivation = Seq(TypeName.write(named), foreign))
    assert(draco.DracoAspect.parents(aspect) == Seq(named))
    assert(draco.DracoAspect.foreignParents(aspect) == Seq(foreign))
    assert(aspect.asJson.hcursor.downField("derivation").focus.contains(Json.arr(named.asJson, foreign.json)))
    assert(aspect.asJson.as[draco.DracoAspect].toOption.get.derivation == aspect.derivation)
  }

  test("neutral TypeName reading matches the existing codec on missing and malformed fields") {
    val cases = Seq("{}", "null", "[]", "42", "{\"name\":null}", "{\"name\":42}",
      "{\"namePackage\":[\"draco\",null]}", "{\"typeParameters\":42}",
      "{\"typeParameters\":[null,{\"json\":1}]}")
    cases.foreach { text =>
      val json = io.circe.parser.parse(text).toOption.get
      val expected = json.as[TypeName].toOption
      val actual = TypeName.read(Dracon(json)).fold(Option.empty[TypeName], value => Some(value))
      assert(actual == expected, text)
    }
  }

  test("undeclared Dracon fields retain wrapper encoding") {
    val td = draco.TypeDefinition(TypeName("External", Seq("example")),
      _dracoAspect = draco.DracoAspect(
        _elements = Seq(draco.Fixed("value", Dracon.string("draco.format.dracon.Dracon"))),
        _factory = draco.Factory(Dracon.string("External"), Seq(draco.Parameter("value", Dracon.string("draco.format.dracon.Dracon"), Dracon.Null)))))
    val generated = draco.DracoGenerator.generate(td)
    assert(!generated.contains("CirceCodecs.encodePayload"))
    assert(!generated.contains("CirceCodecs.decodePayload"))
    assert(generated.contains("x.value.asJson"))
  }

  private def payloadRecord: TypeDefinition = draco.Drake.parse("""type PayloadRecord
    |  elements
    |    fix atom draco.format.dracon.Dracon
    |    fix many [draco.format.dracon.Dracon]
    |    fix wrapped draco.format.dracon.Dracon
    |  factory
    |    parameters
    |      par atom draco.format.dracon.Dracon
    |      par many [draco.format.dracon.Dracon]
    |      par wrapped draco.format.dracon.Dracon
    |domain payload fixture Fixture
    |codec
    |  payload [atom, many]
    |""".stripMargin)

  test("declared payload fields on a non-core owner compile and preserve scalar and sequence wire values") {
    val generated = draco.DracoGenerator.generate(payloadRecord)
    val probe = """
      |trait Fixture extends draco.DracoType
      |object PayloadProbe {
      |  def verify(): Boolean = {
      |    val nodes = Seq(Json.Null, Json.fromString(""), Json.fromBoolean(false), Json.fromInt(0),
      |      Json.arr(Json.Null), Json.obj("json" -> Json.fromInt(7)))
      |    nodes.forall { raw =>
      |      val value = draco.format.dracon.Dracon(raw)
      |      val record = PayloadRecord(value, nodes.map(draco.format.dracon.Dracon(_)), value)
      |      val wire = PayloadRecord.encoder(record)
      |      val expected = Json.obj("atom" -> raw, "many" -> Json.fromValues(nodes), "wrapped" -> Json.obj("json" -> raw))
      |      val decoded = PayloadRecord.decoder.decodeJson(wire).toOption.get
      |      wire == expected && decoded.atom == value && decoded.many.map(_.json) == nodes && decoded.wrapped == value
      |    }
      |  }
      |}
      |""".stripMargin
    val compiled = draco.DracoGenerator.compile(generated + probe, "PayloadRecord.scala")
    assert(compiled.isRight, compiled.left.toOption.getOrElse(Seq.empty).mkString("\n"))
    val clazz = draco.DracoGenerator.loadClass(compiled.toOption.get, "payload.fixture.PayloadProbe$")
    assert(clazz.getMethod("verify").invoke(clazz.getField("MODULE$").get(null)) == java.lang.Boolean.TRUE)
  }

  test("payload declarations reject duplicates, unknown fields and incompatible field types") {
    def withCodec(names: Seq[String]): TypeDefinition = TypeDefinition(payloadRecord.typeName,
      _dracoAspect = payloadRecord.dracoAspect, _domainAspect = payloadRecord.domainAspect,
      _codecAspect = CodecAspect(_payload = names))
    Seq(Seq("atom", "atom"), Seq("missing")).foreach { names =>
      intercept[IllegalArgumentException](draco.DracoGenerator.generate(withCodec(names)))
    }
    val wrong = draco.Drake.parse("""type Wrong
      |  elements
      |    fix atom draco.drake.Text
      |  factory
      |    parameters
      |      par atom draco.drake.Text
      |domain payload fixture Fixture
      |codec
      |  payload [atom]
      |""".stripMargin)
    assert(intercept[IllegalArgumentException](draco.DracoGenerator.generate(wrong)).getMessage.contains("must be Dracon"))
  }

  test("core payload representation comes from owner declarations and survives the TypeElement family") {
    assert(TypeName.typeDefinition.codecAspect.payload == Seq("typeParameters"))
    assert(draco.DracoAspect.typeDefinition.codecAspect.payload == Seq("derivation"))
    assert(draco.TypeElement.typeDefinition.codecAspect.payload == Seq("valueType", "value"))
    val original = TypeName.typeDefinition
    val undeclared = TypeDefinition(original.typeName, _dracoAspect = original.dracoAspect, _domainAspect = original.domainAspect)
    val generated = draco.DracoGenerator.generate(undeclared)
    assert(!generated.contains("CirceCodecs.encodePayload"))
    assert(generated.contains("x.typeParameters.asJson"))
    val value = draco.Fixed("x", Dracon.string("Text"), Dracon.string("body"))
    val wire = draco.TypeElement.encoder(value)
    assert(wire.hcursor.downField("value").focus.contains(Json.fromString("body")))
    assert(draco.TypeElement.decoder.decodeJson(wire).toOption.get.value == value.value)
  }

  test("a non-core sealed family inherits payload representation from the field owner") {
    val root = draco.Drake.parse("""type PayloadRoot
      |  modules [ PayloadLeaf ]
      |  elements
      |    fix atom draco.format.dracon.Dracon
      |domain payload family Family
      |codec
      |  payload [atom]
      |""".stripMargin)
    val leaf = draco.Drake.parse("""type PayloadLeaf from PayloadRoot
      |  factory
      |    parameters
      |      par atom draco.format.dracon.Dracon
      |    body
      |      fix atom draco.format.dracon.Dracon _atom
      |      fix typeDefinition TypeDefinition PayloadLeaf.typeDefinition
      |domain payload family Family
      |""".stripMargin)
    val generated = draco.DracoGenerator.generate(Seq(root, leaf))
    val probe = """
      |trait Family extends draco.DracoType
      |object FamilyProbe {
      |  def verify(): Boolean = {
      |    val raw = Json.obj("json" -> Json.fromInt(7))
      |    val wire = PayloadLeaf.encoder(PayloadLeaf(draco.format.dracon.Dracon(raw)))
      |    wire.hcursor.downField("atom").focus.contains(raw) &&
      |      PayloadLeaf.decoder.decodeJson(wire).toOption.get.atom.json == raw
      |  }
      |}
      |""".stripMargin
    val compiled = draco.DracoGenerator.compile(generated + probe, "PayloadFamily.scala")
    assert(compiled.isRight, compiled.left.toOption.getOrElse(Seq.empty).mkString("\n"))
    val clazz = draco.DracoGenerator.loadClass(compiled.toOption.get, "payload.family.FamilyProbe$")
    assert(clazz.getMethod("verify").invoke(clazz.getField("MODULE$").get(null)) == java.lang.Boolean.TRUE)
    val unboundRoot = TypeDefinition(root.typeName, _dracoAspect = root.dracoAspect, _domainAspect = root.domainAspect)
    val reboundLeaf = TypeDefinition(leaf.typeName, _dracoAspect = leaf.dracoAspect,
      _domainAspect = leaf.domainAspect, _codecAspect = CodecAspect(_payload = Seq("atom")))
    assert(intercept[IllegalArgumentException](draco.DracoGenerator.generate(Seq(unboundRoot, reboundLeaf))).getMessage.contains("field owner"))
  }
}
