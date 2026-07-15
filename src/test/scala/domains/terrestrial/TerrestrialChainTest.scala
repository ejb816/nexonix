package domains.terrestrial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

/** The Terrestrial medium as a Phase-1 SourceSink: the `Creator` receives a seed
  * `MarchIntent`, its `OriginateReport` rule originates a `LocationReport`
  * (900 ft -> 274 m) and sends it — from the rule's RHS over the Evrete Environment
  * seam — to the medium's `Consumer`, which records it. Its representation diverges
  * from the other media (type/unit/elevationMetres); cross-medium transforms live in
  * `World` (Phase 2), not inside this Format domain.
  */
class TerrestrialChainTest extends AnyFunSuite {

  private def intent(unit: String, elevationFeet: Int): MarchIntent = {
    val payload = Json.obj(
      "order"         -> Json.fromString("DEPLOY"),
      "unit"          -> Json.fromString(unit),
      "elevationFeet" -> Json.fromInt(elevationFeet)
    )
    new MarchIntent {
      override lazy val typeDefinition: TypeDefinition = MarchIntent.typeDefinition
      override val json: Json = payload
    }
  }

  test("Creator originates a LocationReport from a MarchIntent (elevationFeet -> elevationMetres) and the Consumer records it") {
    TerrestrialSink.clear()

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

    val system = ActorSystem(guardian, "terrestrialCreation")
    system ! intent("NX5500", 900)   // 900 ft -> 274 m
    Thread.sleep(300)
    system.terminate()

    val recorded = TerrestrialSink.recorded
    assert(recorded.exists(_.contains("NX5500")))
    assert(recorded.exists(_.contains("274")))
  }
}
