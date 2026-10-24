package domains.marine

import draco._
import io.circe.Json
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

class OriginateReportTest extends AnyFunSuite {
  test("OriginateReport's Drake, JSON and generated Scala agree exactly") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/marine/OriginateReport"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/marine/OriginateReport.scala"))
  }

  for ((label, input, vessel, depth) <- Seq(
    ("normal input", Json.obj("vessel" -> Json.fromString("MV100"), "depthMetres" -> Json.fromInt(18288)), "MV100", 10000),
    ("fractional fathoms truncate", Json.obj("vessel" -> Json.fromString("MV10"), "depthMetres" -> Json.fromInt(10)), "MV10", 5),
    ("missing fields", Json.obj(), "UNKNOWN", 0))) {
    test(s"OriginateReport preserves the consumer contract for $label") {
      val received = scala.collection.mutable.ArrayBuffer[draco.format.dracon.Dracon]()
      val consumer: draco.format.dracon.Dracon => Unit = received += _
      val knowledge = Rule.knowledgeService.newKnowledge("MarineOriginate" + depth)
      OriginateReport.ruleType.pattern(knowledge)
      val session = knowledge.newStatefulSession()
      val intent = new VoyageIntent {
        override lazy val typeDefinition: TypeDefinition = VoyageIntent.typeDefinition
        override lazy val json: Json = input
      }
      try {
        session.set("consumer", consumer)
        session.insert(Seq(intent): _*)
        session.fire()
      } finally session.close()
      assert(received.size == 1)
      assert(received.head.isInstanceOf[FixReport])
      assert(received.head.typeDefinition.typeName == FixReport.typeDefinition.typeName)
      assert(received.head.json == Json.obj("kind" -> Json.fromString("FIX"),
        "vessel" -> Json.fromString(vessel), "depthFathoms" -> Json.fromInt(depth)))
    }
  }
}
