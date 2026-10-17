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
}
