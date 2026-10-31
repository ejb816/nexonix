package domains.terrestrial

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

class OriginateReportTest extends AnyFunSuite {
  test("OriginateReport's Drake, JSON and generated Scala agree exactly") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/terrestrial/OriginateReport"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/terrestrial/OriginateReport.scala"))
  }

  for ((label, input, unit, elevation) <- Seq(
    ("normal input", Json.obj("unit" -> Json.fromString("Unit100"), "elevationFeet" -> Json.fromInt(10000)), "Unit100", 3048),
    ("positive fractional metres truncate", Json.obj("unit" -> Json.fromString("Unit10"), "elevationFeet" -> Json.fromInt(10)), "Unit10", 3),
    ("negative fractional metres truncate toward zero", Json.obj("unit" -> Json.fromString("BelowSea"), "elevationFeet" -> Json.fromInt(-10)), "BelowSea", -3),
    ("missing fields", Json.obj(), "UNKNOWN", 0))) {
    test(s"OriginateReport preserves the consumer contract for $label") {
      val received = scala.collection.mutable.ArrayBuffer[draco.format.dracon.Dracon]()
      val consumer: draco.format.dracon.Dracon => Unit = received += _
      val knowledge = Rule.knowledgeService.newKnowledge("TerrestrialOriginate" + elevation)
      OriginateReport.ruleType.pattern(knowledge)
      val session = knowledge.newStatefulSession()
      val intent = MarchIntent(input)
      try {
        session.set("consumer", consumer)
        session.insert(Seq(intent): _*)
        session.fire()
      } finally session.close()
      assert(received.size == 1)
      assert(received.head.isInstanceOf[LocationReport])
      assert(received.head.typeDefinition.typeName == LocationReport.typeDefinition.typeName)
      assert(received.head.json == Json.obj("type" -> Json.fromString("LOCATION"),
        "unit" -> Json.fromString(unit), "elevationMetres" -> Json.fromInt(elevation)))
    }
  }
}
