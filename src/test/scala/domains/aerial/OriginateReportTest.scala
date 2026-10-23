package domains.aerial

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

class OriginateReportTest extends AnyFunSuite {
  test("OriginateReport's Drake, JSON and generated Scala agree exactly") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/aerial/OriginateReport"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/aerial/OriginateReport.scala"))
  }

  for ((label, input, callsign, altitude) <- Seq(
    ("normal input", Json.obj("callsign" -> Json.fromString("NX390"), "flightLevel" -> Json.fromInt(390)), "NX390", 39000),
    ("missing fields", Json.obj(), "UNKNOWN", 0))) {
    test(s"OriginateReport preserves the consumer contract for $label") {
      val received = scala.collection.mutable.ArrayBuffer[draco.format.dracon.Dracon]()
      val consumer: draco.format.dracon.Dracon => Unit = received += _
      val knowledge = Rule.knowledgeService.newKnowledge("AerialOriginate" + altitude)
      OriginateReport.ruleType.pattern(knowledge)
      val session = knowledge.newStatefulSession()
      val intent = new FlightIntent {
        override lazy val typeDefinition: TypeDefinition = FlightIntent.typeDefinition
        override lazy val json: Json = input
      }
      try {
        session.set("consumer", consumer)
        session.insert(Seq(intent): _*)
        session.fire()
      } finally session.close()
      assert(received.size == 1)
      assert(received.head.isInstanceOf[PositionReport])
      assert(received.head.typeDefinition.typeName == PositionReport.typeDefinition.typeName)
      assert(received.head.json == Json.obj("message" -> Json.fromString("POSITION"),
        "callsign" -> Json.fromString(callsign), "altitudeFeet" -> Json.fromInt(altitude)))
    }
  }
}
