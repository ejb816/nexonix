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

  test("payload binding is limited to core definition fields") {
    val td = draco.TypeDefinition(TypeName("External", Seq("example")),
      _dracoAspect = draco.DracoAspect(
        _elements = Seq(draco.Fixed("value", Dracon.string("draco.format.dracon.Dracon"))),
        _factory = draco.Factory(Dracon.string("External"), Seq(draco.Parameter("value", Dracon.string("draco.format.dracon.Dracon"), Dracon.Null)))))
    val generated = draco.DracoGenerator.generate(td)
    assert(!generated.contains("CirceCodecs.encodePayload"))
    assert(!generated.contains("CirceCodecs.decodePayload"))
    assert(generated.contains("x.value.asJson"))
  }
}
