package domains.world

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

class CartesianProjectionTest extends AnyFunSuite {
  private def coordinates(value: Cartesian): (Double, Double, Double) = (value.x, value.y, value.z)

  test("Cartesian's Drake, JSON and generated Scala agree exactly") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/world/Cartesian"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/world/Cartesian.scala"))
  }

  test("scalar defaults and Null retain zero coordinates") {
    assert(coordinates(Cartesian()) == (0.0, 0.0, 0.0))
    assert(coordinates(Cartesian(_y = 2.5)) == (0.0, 2.5, 0.0))
    assert(coordinates(Cartesian.Null) == (0.0, 0.0, 0.0))
    assert(Cartesian.Null.typeDefinition eq Cartesian.typeDefinition)
  }

  test("scalar factory arguments remain independent, lazy and evaluated once") {
    var reads = Vector.empty[String]
    def coordinate(name: String, value: Double): Double = { reads :+= name; value }
    val value = Cartesian(coordinate("x", 1.0), coordinate("y", 2.0), coordinate("z", 3.0))
    assert(reads.isEmpty)
    assert(value.y == 2.0 && value.y == 2.0)
    assert(reads == Vector("y"))
    assert(value.x == 1.0 && value.x == 1.0)
    assert(reads == Vector("y", "x")) // z is never demanded
  }

  test("tuple factory evaluates its argument once at the call and preserves all coordinates") {
    var reads = 0
    def tuple(): (Double, Double, Double) = { reads += 1; (4.5, -5.5, 6.5) }
    val value = Cartesian(tuple())
    assert(reads == 1)
    assert(coordinates(value) == (4.5, -5.5, 6.5))
    assert(coordinates(value) == (4.5, -5.5, 6.5))
    assert(reads == 1)
    assert(value.typeDefinition eq Cartesian.typeDefinition)
  }

  test("generated codecs round-trip the x/y/z object") {
    val value = Cartesian((1.25, -2.5, 3.75))
    val json = Cartesian.encoder(value)
    assert(json == Json.obj("x" -> Json.fromDoubleOrNull(1.25),
      "y" -> Json.fromDoubleOrNull(-2.5), "z" -> Json.fromDoubleOrNull(3.75)))
    val decoded = Cartesian.decoder.decodeJson(json).fold(throw _, identity)
    assert(coordinates(decoded) == coordinates(value))
    assert(decoded.typeDefinition eq Cartesian.typeDefinition)
  }

  test("generated decoder supplies zero for missing coordinates and rejects invalid values") {
    assert(coordinates(Cartesian.decoder.decodeJson(Json.obj()).fold(throw _, identity)) == (0.0, 0.0, 0.0))
    val partial = Json.obj("y" -> Json.fromDoubleOrNull(2.5))
    assert(coordinates(Cartesian.decoder.decodeJson(partial).fold(throw _, identity)) == (0.0, 2.5, 0.0))
    assert(Cartesian.decoder.decodeJson(Json.obj("x" -> Json.fromString("invalid"))).isLeft)
  }
}
