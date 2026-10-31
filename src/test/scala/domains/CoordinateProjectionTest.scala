package domains

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

/** Aerial Position and terrestrial Location, the two coordinate types of the example chain, carry
  * their drake surfaces and the generator's companions, which infer a codec from their three
  * elements as Cartesian's does: each field encoded by name, a missing field decoded to the
  * factory's default. Each case iterates over the two.
  */
class CoordinateProjectionTest extends AnyFunSuite {
  private def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")

  /** medium, type name, the companion's typeDefinition, its third element, a constructor over the
    * three values, the three values of an instance, its codec pair.
    */
  private case class Coordinate(
    medium: String,
    name: String,
    definition: TypeDefinition,
    third: String,
    make: (=> Double, => Double, => Int) => DracoType,
    values: DracoType => (Double, Double, Int),
    encode: DracoType => Json,
    decode: Json => Either[Throwable, DracoType])

  private val coordinates: Seq[Coordinate] = Seq(
    Coordinate("aerial", "Position", domains.aerial.Position.typeDefinition, "altitudeFeet",
      (a, b, c) => domains.aerial.Position(a, b, c),
      { case p: domains.aerial.Position => (p.latitude, p.longitude, p.altitudeFeet) },
      { case p: domains.aerial.Position => domains.aerial.Position.encoder(p) },
      json => domains.aerial.Position.decoder.decodeJson(json)),
    Coordinate("terrestrial", "Location", domains.terrestrial.Location.typeDefinition, "elevationMetres",
      (a, b, c) => domains.terrestrial.Location(a, b, c),
      { case l: domains.terrestrial.Location => (l.latitude, l.longitude, l.elevationMetres) },
      { case l: domains.terrestrial.Location => domains.terrestrial.Location.encoder(l) },
      json => domains.terrestrial.Location.decoder.decodeJson(json)))

  test("each coordinate type's Drake, JSON and generated Scala agree exactly") {
    for (c <- coordinates) {
      val stem = s"src/mods/resources/domains/${c.medium}/${c.name}"
      val source = read(stem + ".drake")
      val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
      assert(Drake.emit(td) == source, c.name)
      assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td), c.name)
      assert(DracoGenerator.generate(td) == read(s"src/mods/scala/domains/${c.medium}/${c.name}.scala"), c.name)
      assert(td.dracoAspect.elements.map(_.name) == Seq("latitude", "longitude", c.third), c.name)
      assert(td.dracoAspect.factory.parameters.map(p => TypeForm.text(p.value.json)) == Seq("0.0", "0.0", "0"), c.name)
      assert(TypeDefinition.encoder(c.definition) == TypeDefinition.encoder(td), c.name)
    }
  }

  test("each factory keeps lazy, once-evaluated arguments, zero defaults and a zero Null") {
    for (c <- coordinates) {
      var evaluated = 0
      val value = c.make({ evaluated += 1; 51.5 }, -0.125, 300)
      assert(evaluated == 0, c.name)
      assert(value.typeDefinition eq c.definition, c.name)
      assert(c.values(value) == ((51.5, -0.125, 300)), c.name)
      assert(c.values(value)._1 == 51.5 && evaluated == 1, c.name)
    }
    assert((domains.aerial.Position.Null.latitude, domains.aerial.Position.Null.longitude, domains.aerial.Position.Null.altitudeFeet) == ((0.0, 0.0, 0)))
    assert((domains.terrestrial.Location.Null.latitude, domains.terrestrial.Location.Null.longitude, domains.terrestrial.Location.Null.elevationMetres) == ((0.0, 0.0, 0)))
    assert(c3(domains.aerial.Position(_longitude = 2.5)) == ((0.0, 2.5, 0)))
  }

  private def c3(p: domains.aerial.Position): (Double, Double, Int) = (p.latitude, p.longitude, p.altitudeFeet)

  test("each inferred codec writes the three fields by name and decodes a missing field to its default") {
    for (c <- coordinates) {
      val value = c.make(51.5, -0.125, 300)
      val json = c.encode(value)
      assert(json == Json.obj("latitude" -> Json.fromDoubleOrNull(51.5), "longitude" -> Json.fromDoubleOrNull(-0.125),
        c.third -> Json.fromInt(300)), c.name)
      val decoded = c.decode(json).fold(throw _, identity)
      assert(c.values(decoded) == ((51.5, -0.125, 300)), c.name)
      assert(decoded.typeDefinition eq c.definition, c.name)
      assert(c.values(c.decode(Json.obj()).fold(throw _, identity)) == ((0.0, 0.0, 0)), c.name)
      assert(c.values(c.decode(Json.obj(c.third -> Json.fromInt(7))).fold(throw _, identity)) == ((0.0, 0.0, 7)), c.name)
      assert(c.decode(Json.obj("latitude" -> Json.fromString("invalid"))).isLeft, c.name)
    }
  }
}
