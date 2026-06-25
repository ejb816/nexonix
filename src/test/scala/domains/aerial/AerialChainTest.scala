package domains.aerial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

/** The Aerial medium as a Phase-1 SourceSink, now with a STATEFUL `Consumer`: the
  * `Creator` receives seed `FlightIntent`s, its `OriginateReport` rule originates a
  * `PositionReport` each (FLnnn -> nnn00 ft) and sends it — from the rule's RHS over
  * the Evrete Environment seam — to the medium's `Consumer`.
  *
  * The `Consumer` is multi-instance stateful: `setupAction` stands up a persistent
  * per-instance session plus an accumulation buffer; `messageAction` inserts+fires
  * each report into it (the `ConsumeReport` rule appends to the buffer); and
  * `signalAction` reaps the accumulated buffer to `AerialSink` at `PostStop`. So the
  * sink fills once, on shutdown — hence the test awaits termination before asserting.
  */
class AerialChainTest extends AnyFunSuite {

  private def intent(callsign: String, flightLevel: Int): FlightIntent = {
    val payload = Json.obj(
      "intent"      -> Json.fromString("DEPART"),
      "callsign"    -> Json.fromString(callsign),
      "flightLevel" -> Json.fromInt(flightLevel)
    )
    new FlightIntent {
      override lazy val typeDefinition: TypeDefinition = FlightIntent.typeDefinition
      override val value: Json = payload
    }
  }

  test("the stateful Consumer accumulates originated PositionReports and reaps them at PostStop") {
    AerialSink.clear()

    val guardian: Behavior[draco.format.json.Json] = Behaviors.setup { ctx =>
      val consumer: ActorRef[draco.format.json.Json] =
        ctx.spawn(Consumer.actorType().asInstanceOf[Actor[draco.format.json.Json]], "consumer")
      val creator: ActorRef[draco.format.json.Json] =
        ctx.spawn(Creator.actorType(consumer).asInstanceOf[Actor[draco.format.json.Json]], "creator")
      Behaviors.receiveMessage { msg =>
        creator ! msg
        Behaviors.same
      }
    }

    val system = ActorSystem(guardian, "aerialCreation")
    system ! intent("NX5500", 390)   // FL390 -> 39000 ft
    system ! intent("NX6600", 350)   // FL350 -> 35000 ft
    Thread.sleep(300)                // let both originate and accumulate in the Consumer's session
    system.terminate()
    Await.result(system.whenTerminated, 3.seconds)  // PostStop reaps the accumulated reports

    val recorded = AerialSink.recorded
    assert(recorded.exists(_.contains("NX5500")))
    assert(recorded.exists(_.contains("39000")))
    assert(recorded.exists(_.contains("NX6600")))
    assert(recorded.exists(_.contains("35000")))
  }
}
