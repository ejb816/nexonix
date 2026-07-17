package domains.aerial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** Behavioral test — the generated stateful `Consumer` actor + `ConsumeReport`
  * rule, in isolation (no cross-actor sends; the wired chain is `AerialAssemblyTest`).
  *
  * Two angles, mirroring the Primes precedent:
  *  - rule-direct: build a session, seed the `consumed` buffer the way the actor's
  *    `start` action does, insert a `PositionReport`, fire, observe the buffer —
  *    validates the rule's logic (append to the session's consumed buffer) independent
  *    of any actor;
  *  - actor-loose: send a `PositionReport` to `Consumer.actorType()`, stop it, and
  *    observe the sink — validates the *generated* stateful actor: `start` stands
  *    up the session + buffer, `message` accumulates, `signal` reaps to the
  *    sink at `PostStop`. So the sink fills on stop — hence the await.
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
      override val json: Json = payload
    }
  }

  test("ConsumeReport rule appends an inserted report to the session's consumed buffer") {
    val knowledge = Rule.knowledgeService.newKnowledge("AerialConsumeRuleTest")
    ConsumeReportRule.ruleType.pattern.accept(knowledge)
    val session = knowledge.newStatefulSession()
    val consumed = new java.util.ArrayList[String]()
    session.set("consumed", consumed)
    session.insert(Seq(report("NX1042", 35000)): _*)
    session.fire()
    session.close()
    assert(consumed.asScala.exists(_.contains("NX1042")))
  }

  test("Consumer actor accumulates a PositionReport and reaps it to the sink at PostStop") {
    AerialSink.clear()
    val system = ActorSystem(Consumer.actorType().asInstanceOf[Actor[draco.format.json.JSON]], "aerialConsumer")
    system ! report("NX2087", 28000)
    Thread.sleep(200)
    system.terminate()
    Await.result(system.whenTerminated, 3.seconds)
    assert(AerialSink.recorded.exists(_.contains("NX2087")))
  }
}
