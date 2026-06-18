package domains.aerial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

/** The Aerial medium as a Phase-1 SourceSink: the `Creator` receives a seed
  * `FlightIntent`, its `OriginateReport` rule originates a `PositionReport`
  * (FL390 -> 39000 ft) and sends it — from inside the rule's RHS, over the Evrete
  * Environment seam — to the medium's `Consumer`, which records it.
  *
  * The per-medium relay is gone: cross-medium transforms live in `World` (Phase 2),
  * not inside a Format domain. What remains is the rule-RHS -> ActorRef send (the
  * pattern proven in 6c) carrying an originated message from Creator to Consumer.
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

  test("Creator originates a PositionReport from a FlightIntent and the Consumer records it") {
    AerialSink.clear()

    val guardian: Behavior[draco.format.json.Json] = Behaviors.setup { ctx =>
      val consumer: ActorRef[draco.format.json.Json] =
        ctx.spawn(Consumer.actorType.asInstanceOf[Actor[draco.format.json.Json]], "consumer")
      val creator: ActorRef[draco.format.json.Json] =
        ctx.spawn(Creator.actorType(consumer).asInstanceOf[Actor[draco.format.json.Json]], "creator")
      Behaviors.receiveMessage { msg =>
        creator ! msg
        Behaviors.same
      }
    }

    val system = ActorSystem(guardian, "aerialCreation")
    system ! intent("NX5500", 390)   // FL390 -> 39000 ft
    Thread.sleep(300)
    system.terminate()

    val recorded = AerialSink.recorded
    assert(recorded.exists(_.contains("NX5500")))
    assert(recorded.exists(_.contains("39000")))
  }
}
