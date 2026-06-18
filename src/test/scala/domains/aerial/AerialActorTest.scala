package domains.aerial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

/** Sub-step 6b behavioral test — the generated `Consumer` actor + `ConsumeReport`
  * rule, in isolation (no cross-actor sends; that is 6c).
  *
  * Two angles, mirroring the Primes precedent:
  *  - rule-direct: build a session, insert a `PositionReport`, fire, observe the
  *    sink — validates the rule's logic independent of any actor;
  *  - actor-loose: send a `PositionReport` to `Consumer.actorType` and observe the
  *    sink — validates the *generated* actor: its `knowledge` built from
  *    `elementTypeNames`, and its thin-membrane receive (insert → fire).
  */
class AerialActorTest extends AnyFunSuite {

  private def report(callsign: String, altitudeFeet: Int): PositionReport = {
    val payload = Json.obj(
      "message"      -> Json.fromString("POSITION"),
      "callsign"     -> Json.fromString(callsign),
      "altitudeFeet" -> Json.fromInt(altitudeFeet)
    )
    new PositionReport {
      override lazy val typeDefinition: TypeDefinition = PositionReport.typeDefinition
      override val value: Json = payload
    }
  }

  test("ConsumeReport rule records a report inserted directly into a session") {
    AerialSink.clear()
    val knowledge = Rule.knowledgeService.newKnowledge("AerialConsumeRuleTest")
    ConsumeReportRule.ruleType.pattern.accept(knowledge)
    val session = knowledge.newStatefulSession()
    session.insert(Seq(report("NX1042", 35000)): _*)
    session.fire()
    session.close()
    assert(AerialSink.recorded.exists(_.contains("NX1042")))
  }

  test("Consumer actor consumes a PositionReport via its generated behavior") {
    AerialSink.clear()
    val system = ActorSystem(Consumer.actorType.asInstanceOf[Actor[draco.format.json.Json]], "aerialConsumer")
    system ! report("NX2087", 28000)
    Thread.sleep(200)
    system.terminate()
    assert(AerialSink.recorded.exists(_.contains("NX2087")))
  }
}
