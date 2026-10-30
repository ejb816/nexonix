package domains

import draco._
import io.circe.Json
import io.circe.syntax.EncoderOps
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

/** The three sibling reports — aerial PositionReport, marine FixReport, ethereal EphemerisReport —
  * carry the same `json` factory terrestrial LocationReport took first, so each medium's
  * OriginateReport constructs its report as `X(payload)` instead of an anonymous host construction.
  * Each case iterates over the three: the trio, the by-name argument, the codec the Dracon shape derives.
  */
class ReportFactoryTest extends AnyFunSuite {
  private type Dracon = draco.format.dracon.Dracon

  private def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")

  /** medium, report name, the companion's typeDefinition, its factory, its Null, the domain marker. */
  private case class Report(
    medium: String,
    name: String,
    definition: TypeDefinition,
    make: (=> Json) => Dracon,
    nullValue: Dracon,
    isMedium: Dracon => Boolean)

  private val reports: Seq[Report] = Seq(
    Report("aerial", "PositionReport", domains.aerial.PositionReport.typeDefinition,
      json => domains.aerial.PositionReport(json), domains.aerial.PositionReport.Null, _.isInstanceOf[domains.aerial.Aerial]),
    Report("marine", "FixReport", domains.marine.FixReport.typeDefinition,
      json => domains.marine.FixReport(json), domains.marine.FixReport.Null, _.isInstanceOf[domains.marine.Marine]),
    Report("ethereal", "EphemerisReport", domains.ethereal.EphemerisReport.typeDefinition,
      json => domains.ethereal.EphemerisReport(json), domains.ethereal.EphemerisReport.Null, _.isInstanceOf[domains.ethereal.Ethereal]))

  private val payload: Json = Json.obj(("kind", Json.fromString("PROBE")), ("value", Json.fromInt(42)))

  test("each sibling report's Drake, JSON and generated Scala agree exactly and declare the json factory") {
    for (r <- reports) {
      val stem = s"src/mods/resources/domains/${r.medium}/${r.name}"
      val source = read(stem + ".drake")
      val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
      assert(Drake.emit(td) == source, r.name)
      assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td), r.name)
      assert(DracoGenerator.generate(td) == read(s"src/mods/scala/domains/${r.medium}/${r.name}.scala"), r.name)
      assert(td.dracoAspect.factory.parameters.map(p => (p.name, TypeForm.text(p.valueType.json))) == Seq(("json", "Json")), r.name)
      assert(DracoAspect.parents(td.dracoAspect).map(_.name) == Seq(r.medium.capitalize, "Dracon"), r.name)
      assert(TypeDefinition.encoder(r.definition) == TypeDefinition.encoder(td), r.name)
    }
  }

  test("each factory argument is evaluated once, on first read, and never when unused") {
    for (r <- reports) {
      var evaluated = 0
      val report = r.make({ evaluated += 1; payload })
      assert(evaluated == 0, r.name)
      assert(report.typeDefinition eq r.definition, r.name)
      assert(r.isMedium(report), r.name)
      assert(evaluated == 0, r.name)
      assert(report.json == payload, r.name)
      assert(report.json.hcursor.get[Int]("value").contains(42), r.name)
      assert(evaluated == 1, r.name)
      assert(r.nullValue.json == Json.Null, r.name)
    }
  }

  test("each derived codec writes the json field and reads the report back") {
    val encoded = domains.aerial.PositionReport(payload).asJson
    assert(encoded == Json.obj(("json", payload)))
    val position = encoded.as[domains.aerial.PositionReport].fold(throw _, identity)
    assert(position.json == payload && (position.typeDefinition eq domains.aerial.PositionReport.typeDefinition))
    val fix = domains.marine.FixReport(payload).asJson.as[domains.marine.FixReport].fold(throw _, identity)
    assert(fix.json == payload && (fix.typeDefinition eq domains.marine.FixReport.typeDefinition))
    val ephemeris = domains.ethereal.EphemerisReport(payload).asJson.as[domains.ethereal.EphemerisReport].fold(throw _, identity)
    assert(ephemeris.json == payload && (ephemeris.typeDefinition eq domains.ethereal.EphemerisReport.typeDefinition))
    assert(Json.obj().as[domains.marine.FixReport].fold(throw _, identity).json == Json.Null)
  }
}
