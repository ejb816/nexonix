package domains.world

import draco._
import io.circe.{Json, parser}
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

/** Slice B — the first cross-medium transform as a running actor service, end to
  * end. A loose Aerial `PositionReport` crosses to a Terrestrial `LocationReport`
  * through the World transformation service, and "preserves meaning" is asserted at
  * the far sink — the same world point comes out, altitude reframed feet -> metres.
  *
  * The graph (spawned bottom-up so each stage holds its downstream ref):
  *
  *   PositionReport(Json)
  *     -> aerial.Input        [decode]   Json        -> typed Position
  *     -> world.Consumer      [transform] Position   -> Location (via the Observable)
  *     -> world.Provider      [route]     Location   -> target output
  *     -> terrestrial.Output  [encode]    Location   -> LocationReport(Json)
  *     -> terrestrial.Consumer (existing) -> TerrestrialSink
  *
  * The transform interior is the Scala `Geodesy`/`Observable` core proven in
  * `AerialTerrestrialTransformTest`; here it is exercised through the actors.
  */
class WorldTransformServiceTest extends AnyFunSuite {

  private def positionReport(callsign: String, latitude: Double, longitude: Double, altitudeFeet: Int): domains.aerial.PositionReport = {
    val payload = Json.obj(
      "message"      -> Json.fromString("POSITION"),
      "callsign"     -> Json.fromString(callsign),
      "latitude"     -> Json.fromDoubleOrNull(latitude),
      "longitude"    -> Json.fromDoubleOrNull(longitude),
      "altitudeFeet" -> Json.fromInt(altitudeFeet)
    )
    new domains.aerial.PositionReport {
      override lazy val typeDefinition: TypeDefinition = domains.aerial.PositionReport.typeDefinition
      override val value: Json = payload
    }
  }

  test("an Aerial PositionReport crosses to a Terrestrial LocationReport through the World service, meaning preserved") {
    domains.terrestrial.TerrestrialSink.clear()

    val guardian: Behavior[draco.format.json.Json] = Behaviors.setup { ctx =>
      val terrConsumer: ActorRef[draco.format.json.Json] =
        ctx.spawn(domains.terrestrial.Consumer.actorType.asInstanceOf[Actor[draco.format.json.Json]], "terrConsumer")
      val output: ActorRef[World] =
        ctx.spawn(domains.terrestrial.Output.actorType(terrConsumer).asInstanceOf[Actor[World]], "output")
      val provider: ActorRef[World] =
        ctx.spawn(domains.world.Provider.actorType(output).asInstanceOf[Actor[World]], "provider")
      val worldConsumer: ActorRef[World] =
        ctx.spawn(domains.world.Consumer.actorType(provider).asInstanceOf[Actor[World]], "worldConsumer")
      val input: ActorRef[draco.format.json.Json] =
        ctx.spawn(domains.aerial.Input.actorType(worldConsumer).asInstanceOf[Actor[draco.format.json.Json]], "input")

      Behaviors.receiveMessage { msg =>
        input ! msg
        Behaviors.same
      }
    }

    val system = ActorSystem(guardian, "worldTransform")
    system ! positionReport("NX5500", latitude = 51.5, longitude = -0.12, altitudeFeet = 35000)
    Thread.sleep(300)
    system.terminate()

    val recorded = domains.terrestrial.TerrestrialSink.recorded
    assert(recorded.nonEmpty, "Terrestrial sink recorded nothing")

    val out = parser.parse(recorded.head).toOption.get.hcursor
    assert(math.abs(out.get[Double]("latitude").toOption.get  - 51.5)  < 1e-6)
    assert(math.abs(out.get[Double]("longitude").toOption.get - -0.12) < 1e-6)
    assert(out.get[Int]("elevationMetres").toOption.get == 10668)   // 35000 ft -> 10668 m through the canonical
  }
}
