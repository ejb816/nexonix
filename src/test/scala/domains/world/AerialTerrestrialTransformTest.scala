package domains.world

import draco._
import org.scalatest.funsuite.AnyFunSuite

/** First transform vertical (semantic core, no actors yet): an Aerial `Position`
  * crosses to a Terrestrial `Location` through the `Observable` — the world-fact.
  *
  * Both media are Geocentric/Axial and geodetic (the deferred cases: Terrestrial's
  * projected grid, and the Heliocentric/Ecliptic translation + time, are out of this
  * slice). So the input adapter projects geodetic→ECEF, the output adapter ECEF→
  * geodetic, and "preserves meaning" becomes a passing assertion: the same world
  * point comes out, with altitude reframed feet→metres through the canonical.
  */
class AerialTerrestrialTransformTest extends AnyFunSuite {

  test("an Aerial Position transforms to a Terrestrial Location with the Observable (world-fact) preserved") {
    val position = domains.aerial.Position(_latitude = 51.5, _longitude = -0.12, _altitudeFeet = 35000)

    // input adapter: Aerial geodetic (feet) -> Observable (ECEF, Geocentric/Axial)
    val heightMetres = position.altitudeFeet * 0.3048
    val observable   = Observable.fromGeodetic(position.latitude, position.longitude, heightMetres)

    // output adapter: Observable (ECEF) -> Terrestrial geodetic (metres)
    val (lat, lon, h) = Observable.toGeodetic(observable)
    val location      = domains.terrestrial.Location(_latitude = lat, _longitude = lon, _elevationMetres = math.round(h).toInt)

    // meaning preserved: same horizontal world point, altitude reframed feet -> metres
    assert(math.abs(location.latitude - position.latitude) < 1e-6)
    assert(math.abs(location.longitude - position.longitude) < 1e-6)
    assert(location.elevationMetres == math.round(position.altitudeFeet * 0.3048).toInt) // 35000 ft -> 10668 m

    // the Observable's geocentric frame IS the canonical ECEF of the point
    val (ex, ey, ez) = Geodesy.geodeticToEcef(position.latitude, position.longitude, heightMetres)
    assert(observable.geocentric.x == ex && observable.geocentric.y == ey && observable.geocentric.z == ez)

    // and the typed forms are, transitively, World subtypes (the message-domain spine)
    assert(position.isInstanceOf[World] && location.isInstanceOf[World] && observable.isInstanceOf[World])
  }
}
