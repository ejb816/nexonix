package domains.terrestrial

import draco._
import io.circe.Json
import io.circe.syntax.EncoderOps
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

/** `LocationReport` carries a `json` factory in its definition, so the two terrestrial sites that
  * construct it — OriginateReport's action and Output's Location branch — apply the factory,
  * `LocationReport(payload)`, instead of spelling an anonymous host construction. These tests pin
  * the trio, the factory's by-name argument and the codec the Dracon shape derives.
  */
class LocationReportTest extends AnyFunSuite {
  private def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")

  private val payload: Json = Json.obj(
    ("type", Json.fromString("LOCATION")),
    ("latitude", Json.fromDoubleOrNull(51.5)),
    ("longitude", Json.fromDoubleOrNull(-0.12)),
    ("elevationMetres", Json.fromInt(10668)))

  test("LocationReport's Drake, JSON and generated Scala agree exactly and declare the json factory") {
    val stem = "src/mods/resources/domains/terrestrial/LocationReport"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/terrestrial/LocationReport.scala"))
    assert(td.dracoAspect.factory.parameters.map(p => (p.name, TypeForm.text(p.valueType.json))) == Seq(("json", "Json")))
    assert(DracoAspect.parents(td.dracoAspect).map(_.name) == Seq("Terrestrial", "Dracon"))
  }

  test("the factory argument is evaluated once, on first read, and never when unused") {
    var evaluated = 0
    val report = LocationReport({ evaluated += 1; payload })
    assert(evaluated == 0)
    assert(report.typeDefinition eq LocationReport.typeDefinition)
    assert(report.isInstanceOf[Terrestrial] && report.isInstanceOf[draco.format.dracon.Dracon])
    assert(evaluated == 0)
    assert(report.json == payload)
    assert(report.json.hcursor.get[Int]("elevationMetres").contains(10668))
    assert(evaluated == 1)
    assert(LocationReport.Null.json == Json.Null)
  }

  test("the derived codec writes the json field and reads a LocationReport back") {
    val report = LocationReport(payload)
    val encoded = report.asJson
    assert(encoded == Json.obj(("json", payload)))
    val decoded = encoded.as[LocationReport].fold(throw _, identity)
    assert(decoded.json == payload)
    assert(decoded.typeDefinition eq LocationReport.typeDefinition)
    assert(Json.obj().as[LocationReport].fold(throw _, identity).json == Json.Null)
  }
}
