package domains.marine

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

/** The Marine medium as a Phase-1 SourceSink: the `Creator` receives a seed
  * `VoyageIntent`, its `OriginateReport` rule originates a `FixReport`
  * (183 m -> 100 fathoms) and sends it — from the rule's RHS over the Evrete
  * Environment seam — to the medium's `Consumer`, which records it. Its
  * representation diverges from the other media (kind/vessel/depthFathoms);
  * cross-medium transforms live in `World` (Phase 2), not inside this Format domain.
  */
class MarineChainTest extends AnyFunSuite {

  private def intent(vessel: String, depthMetres: Int): VoyageIntent = {
    val payload = Json.obj(
      "tasking"     -> Json.fromString("SAIL"),
      "vessel"      -> Json.fromString(vessel),
      "depthMetres" -> Json.fromInt(depthMetres)
    )
    new VoyageIntent {
      override lazy val typeDefinition: TypeDefinition = VoyageIntent.typeDefinition
      override val json: Json = payload
    }
  }

  test("Creator originates a FixReport from a VoyageIntent (depthMetres -> depthFathoms) and the Consumer records it") {
    MarineSink.clear()

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

    val system = ActorSystem(guardian, "marineCreation")
    system ! intent("NX5500", 183)   // 183 m -> 100 fathoms
    Thread.sleep(300)
    system.terminate()

    val recorded = MarineSink.recorded
    assert(recorded.exists(_.contains("NX5500")))
    assert(recorded.exists(_.contains("100")))
  }
}
