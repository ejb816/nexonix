package draco.base.coordinates

import draco.base.{Meters, Radians, Spherical}
import org.scalatest.funsuite.AnyFunSuite

class SphericalTest extends AnyFunSuite {
  test ("Spherical") {
    val spherical: Spherical = Spherical(Radians(1.0), Radians(2.0), Meters(10.0))
  }
}
