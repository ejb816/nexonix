package domains.world

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

class ObservableProjectionTest extends AnyFunSuite {
  private def coordinates(value: Cartesian): (Double, Double, Double) = (value.x, value.y, value.z)

  test("Observable's Drake, JSON and generated Scala agree exactly") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/world/Observable"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/world/Observable.scala"))
  }

  test("factory defaults and Null retain both Cartesian.Null frames") {
    for (value <- Seq(Observable(), Observable.Null)) {
      assert(value.geocentric eq Cartesian.Null)
      assert(value.heliocentric eq Cartesian.Null)
      assert(value.typeDefinition eq Observable.typeDefinition)
    }
  }

  test("factory frame arguments remain independent, lazy and evaluated once") {
    var reads = Vector.empty[String]
    def frame(name: String): Cartesian = { reads :+= name; Cartesian(1.0, 2.0, 3.0) }
    val value = Observable(frame("geo"), frame("helio"))
    assert(reads.isEmpty)
    val geo = value.geocentric
    assert(value.geocentric eq geo)
    assert(reads == Vector("geo")) // heliocentric is never demanded
  }

  test("fromGeodetic evaluates its scalar arguments strictly once and leaves the other frame at Null") {
    var reads = Vector.empty[String]
    def arg(name: String, value: Double): Double = { reads :+= name; value }
    val value = Observable.fromGeodetic(arg("lat", 0.0), arg("lon", 0.0), arg("height", 25.0))
    assert(reads == Vector("lat", "lon", "height"))
    assert(value.heliocentric eq Cartesian.Null)
    assert(coordinates(value.geocentric) == (6378162.0, 0.0, 0.0))
    assert(reads == Vector("lat", "lon", "height"))
  }

  test("toGeodetic evaluates its argument once and reads only the geocentric frame") {
    var reads = 0
    var frameReads = 0
    def input(): Observable = {
      reads += 1
      Observable({ frameReads += 1; Cartesian(6378137.0, 0.0, 0.0) },
        throw new AssertionError("heliocentric frame must not be demanded"))
    }
    val result = Observable.toGeodetic(input())
    assert(reads == 1 && frameReads == 1)
    assert(result == (0.0, 0.0, 0.0))
  }

  test("geodetic helpers preserve the existing conversion across representative points") {
    for ((lat, lon, height) <- Seq((0.0, 0.0, 0.0), (51.5, -0.12, 10668.0), (-33.86, 151.21, -25.0))) {
      val value = Observable.fromGeodetic(lat, lon, height)
      assert(coordinates(value.geocentric) == Geodesy.geodeticToEcef(lat, lon, height))
      val (actualLat, actualLon, actualHeight) = Observable.toGeodetic(value)
      assert(math.abs(actualLat - lat) < 1e-6)
      assert(math.abs(actualLon - lon) < 1e-6)
      assert(math.abs(actualHeight - height) < 1e-3)
    }
  }

  test("generated nested codecs preserve distinct geocentric and heliocentric frames") {
    val geo = Cartesian(1.25, -2.5, 3.75)
    val helio = Cartesian(-4.5, 5.25, 6.0)
    val json = Observable.encoder(Observable(geo, helio))
    assert(json == Json.obj("geocentric" -> Cartesian.encoder(geo), "heliocentric" -> Cartesian.encoder(helio)))
    val decoded = Observable.decoder.decodeJson(json).fold(throw _, identity)
    assert(coordinates(decoded.geocentric) == coordinates(geo))
    assert(coordinates(decoded.heliocentric) == coordinates(helio))
    assert(decoded.typeDefinition eq Observable.typeDefinition)
  }

  test("generated decoder defaults missing frames and coordinates and rejects invalid nested values") {
    val empty = Observable.decoder.decodeJson(Json.obj()).fold(throw _, identity)
    assert(empty.geocentric eq Cartesian.Null)
    assert(empty.heliocentric eq Cartesian.Null)
    val partial = Json.obj("geocentric" -> Json.obj("x" -> Json.fromDoubleOrNull(2.5)))
    val decoded = Observable.decoder.decodeJson(partial).fold(throw _, identity)
    assert(coordinates(decoded.geocentric) == (2.5, 0.0, 0.0))
    assert(decoded.heliocentric eq Cartesian.Null)
    assert(Observable.decoder.decodeJson(Json.obj("geocentric" -> Json.obj("x" -> Json.fromString("invalid")))).isLeft)
  }
}
