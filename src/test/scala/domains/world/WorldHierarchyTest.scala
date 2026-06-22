package domains.world

import draco._
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

/** The World message-domain spine, realized implicitly via `Domain[T]`: each medium
  * is a subdomain of `World` (it derives it), so a typed message shell — a direct
  * subtype of its medium — is an *indirect* subtype of `World`. This is the "message
  * domain" discipline (contained types subtype the domain) that a future explicit
  * `Message[Domain]` will name and enforce; for now the derivation alone carries it.
  *
  * Foundation only: this fixes the domain hierarchy. World's transform machinery
  * (input/output adapter actors + the meaning-preserving transform rules) and the
  * typed message forms arrive in the next slice.
  */
class WorldHierarchyTest extends AnyFunSuite {

  test("each medium derives World, and its message shells are indirect World subtypes") {
    val aerial = new domains.aerial.PositionReport {
      override lazy val typeDefinition: TypeDefinition = domains.aerial.PositionReport.typeDefinition
      override val value: Json = Json.obj()
    }
    val terrestrial = new domains.terrestrial.LocationReport {
      override lazy val typeDefinition: TypeDefinition = domains.terrestrial.LocationReport.typeDefinition
      override val value: Json = Json.obj()
    }
    val marine = new domains.marine.FixReport {
      override lazy val typeDefinition: TypeDefinition = domains.marine.FixReport.typeDefinition
      override val value: Json = Json.obj()
    }
    val ethereal = new domains.ethereal.EphemerisReport {
      override lazy val typeDefinition: TypeDefinition = domains.ethereal.EphemerisReport.typeDefinition
      override val value: Json = Json.obj()
    }

    // indirect World subtype, via medium
    assert(aerial.isInstanceOf[World])
    assert(terrestrial.isInstanceOf[World])
    assert(marine.isInstanceOf[World])
    assert(ethereal.isInstanceOf[World])

    // still a direct subtype of its own medium, and a Format shell
    assert(aerial.isInstanceOf[domains.aerial.Aerial])
    assert(aerial.isInstanceOf[draco.format.json.Json])
  }

  test("Sentient is a subdomain of World") {
    // Compile-time evidence: this line fails to compile if Sentient does not derive World.
    implicitly[domains.sentient.Sentient <:< World]
  }
}
