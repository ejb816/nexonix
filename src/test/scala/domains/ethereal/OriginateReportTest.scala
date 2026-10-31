package domains.ethereal

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

class OriginateReportTest extends AnyFunSuite {
  test("OriginateReport's Drake, JSON and generated Scala agree exactly") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/ethereal/OriginateReport"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/ethereal/OriginateReport.scala"))
  }

  for ((label, input, obj, altitude) <- Seq(
    ("normal input", Json.obj("object" -> Json.fromString("Orbiter"), "altitudeNauticalMiles" -> Json.fromInt(1000)), "Orbiter", 1852),
    ("positive fractional kilometres truncate", Json.obj("object" -> Json.fromString("Probe"), "altitudeNauticalMiles" -> Json.fromInt(10)), "Probe", 18),
    ("negative fractional kilometres truncate toward zero", Json.obj("object" -> Json.fromString("NegativeAltitude"), "altitudeNauticalMiles" -> Json.fromInt(-10)), "NegativeAltitude", -18),
    ("missing fields", Json.obj(), "UNKNOWN", 0))) {
    test(s"OriginateReport preserves the consumer contract for $label") {
      val received = scala.collection.mutable.ArrayBuffer[draco.format.dracon.Dracon]()
      val consumer: draco.format.dracon.Dracon => Unit = received += _
      val knowledge = Rule.knowledgeService.newKnowledge("EtherealOriginate" + altitude)
      OriginateReport.ruleType.pattern(knowledge)
      val session = knowledge.newStatefulSession()
      val intent = LaunchIntent(input)
      try {
        session.set("consumer", consumer)
        session.insert(Seq(intent): _*)
        session.fire()
      } finally session.close()
      assert(received.size == 1)
      assert(received.head.isInstanceOf[EphemerisReport])
      assert(received.head.typeDefinition.typeName == EphemerisReport.typeDefinition.typeName)
      assert(received.head.json == Json.obj("category" -> Json.fromString("EPHEMERIS"),
        "object" -> Json.fromString(obj), "altitudeKilometres" -> Json.fromInt(altitude)))
    }
  }
}
