package domains.world

import draco._
import org.scalatest.funsuite.AnyFunSuite

/** The World canonical coordinate primitive. A `Cartesian` is the named-field
  * carrier (x/y/z, metres) of one frame of the dual-frame world-fact; this checks
  * the named surface and the positional-value constructor that bridges to draco's
  * `Coordinate` substrate. */
class WorldCanonicalTest extends AnyFunSuite {

  test("Cartesian exposes named x/y/z, and its factory maps a positional coordinate value into the names") {
    val byName = Cartesian(1.0, 2.0, 3.0)
    assert(byName.x == 1.0 && byName.y == 2.0 && byName.z == 3.0)

    val byPosition = Cartesian((4.0, 5.0, 6.0)) // (_1, _2, _3) -> (x, y, z)
    assert(byPosition.x == 4.0 && byPosition.y == 5.0 && byPosition.z == 6.0)

    assert(Cartesian.typeDefinition.typeName.name == "Cartesian")
  }
}
