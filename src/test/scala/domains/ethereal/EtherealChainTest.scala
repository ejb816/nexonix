package domains.ethereal

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

/** The Ethereal medium as a Phase-1 SourceSink: the `Creator` receives a seed
  * `LaunchIntent`, its `OriginateReport` rule originates an `EphemerisReport`
  * (100 nm -> 185 km) and sends it — from the rule's RHS over the Evrete Environment
  * seam — to the medium's `Consumer`, which records it. Its representation diverges
  * from the other media (category/object/altitudeKilometres); cross-medium
  * transforms live in `World` (Phase 2), not inside this Format domain.
  */
class EtherealChainTest extends AnyFunSuite {

  private def intent(obj: String, altitudeNauticalMiles: Int): LaunchIntent = {
    val payload = Json.obj(
      "mission"               -> Json.fromString("LAUNCH"),
      "object"                -> Json.fromString(obj),
      "altitudeNauticalMiles" -> Json.fromInt(altitudeNauticalMiles)
    )
    new LaunchIntent {
      override lazy val typeDefinition: TypeDefinition = LaunchIntent.typeDefinition
      override val json: Json = payload
    }
  }

  test("Creator originates an EphemerisReport from a LaunchIntent (nauticalMiles -> kilometres) and the Consumer records it") {
    EtherealSink.clear()

    val guardian: Behavior[draco.format.json.JSON] = Behaviors.setup { ctx =>
      val consumer: ActorRef[draco.format.json.JSON] =
        ctx.spawn(Consumer.actorType().asInstanceOf[Actor[draco.format.json.JSON]], "consumer")
      val creator: ActorRef[draco.format.json.JSON] =
        ctx.spawn(Creator.actorType(consumer).asInstanceOf[Actor[draco.format.json.JSON]], "creator")
      Behaviors.receiveMessage { msg =>
        creator ! msg
        Behaviors.same
      }
    }

    val system = ActorSystem(guardian, "etherealCreation")
    system ! intent("NX5500", 100)   // 100 nautical miles -> 185 km
    Thread.sleep(300)
    system.terminate()

    val recorded = EtherealSink.recorded
    assert(recorded.exists(_.contains("NX5500")))
    assert(recorded.exists(_.contains("185")))
  }
}
