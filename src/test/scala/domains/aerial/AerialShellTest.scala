package domains.aerial

import draco._
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

/** Sub-step 6a — the Aerial domain + its message-shell layer, in isolation.
  *
  * A `PositionReport` is a *strong handle* (an Aerial domain member, and a
  * `draco.format.json.Json` format) wrapped around a *loose* `io.circe.Json`
  * payload. This proves the Format link compiles and that the shell carries the
  * payload without strongly typing the message's fields — the actor pipeline and
  * rules (sub-step 6b) build on top of this. No Generator actor-emission is
  * exercised here; the domain mirrors `Language`, the shell mirrors `Distance`.
  */
class AerialShellTest extends AnyFunSuite {

  private val payload: Json = Json.obj(
    "message"      -> Json.fromString("POSITION"),
    "callsign"     -> Json.fromString("NX1042"),
    "altitudeFeet" -> Json.fromInt(35000)
  )

  test("PositionReport wraps loose JSON as a strong Aerial/Json shell") {
    val report: PositionReport = new PositionReport {
      override lazy val typeDefinition: TypeDefinition = PositionReport.typeDefinition
      override val value: Json = payload
    }

    // loose payload, read without strong typing
    assert(report.value.hcursor.get[String]("callsign").contains("NX1042"))
    assert(report.value.hcursor.get[Int]("altitudeFeet").contains(35000))

    // strong identity: an Aerial member and a Json format
    assert(report.isInstanceOf[Aerial])
    assert(report.isInstanceOf[draco.format.json.Json])
  }

  test("Aerial declares PositionReport as a member") {
    assert(Aerial.elementTypeNames.contains("PositionReport"))
    assert(PositionReport.typeDefinition.typeName.name == "PositionReport")
  }
}
