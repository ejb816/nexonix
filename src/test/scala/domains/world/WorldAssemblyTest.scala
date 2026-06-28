package domains.world

import draco._
import io.circe.{Json, parser}
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

/** The cross-medium World transform service, expressed as a draco [[Assembly]]
  * and driven by the generic [[AssemblySpawner]].
  *
  * This is the demanding case for the Assembly machinery: a five-member chain that
  * spans three domains and crosses TWO message types —
  *
  *   aerial.Input        Actor[Json]   -- worldConsumer: ActorRef[World]
  *   world.Consumer      Actor[World]  -- provider:      ActorRef[World]
  *   world.Provider      Actor[World]  -- target:        ActorRef[World]
  *   terrestrial.Output  Actor[World]  -- consumer:      ActorRef[Json]
  *   terrestrial.Consumer Actor[Json]  -- (leaf)
  *
  * The wiring is pure data ([[Binding]]s addressed by [[TypeName]]). The validator
  * confirms each binding's `ActorRef[M]` parameter matches the receive type `M` of
  * its target — including the two type flips (`Input`'s `ActorRef[World]` feeding a
  * `World` actor, `Output`'s `ActorRef[Json]` feeding a `Json` actor). The spawner
  * then stands the chain up bottom-up and the end-to-end result matches the
  * hand-wired service: a London `PositionReport` arrives at the Terrestrial sink as
  * a `LocationReport`, altitude reframed 35000 ft -> 10668 m, lat/lon preserved.
  */
class WorldAssemblyTest extends AnyFunSuite {

  private val Input    = domains.aerial.Input.typeDefinition.typeName
  private val WConsumer = domains.world.Consumer.typeDefinition.typeName
  private val WProvider = domains.world.Provider.typeDefinition.typeName
  private val Output   = domains.terrestrial.Output.typeDefinition.typeName
  private val TConsumer = domains.terrestrial.Consumer.typeDefinition.typeName

  private val worldService: Assembly = Assembly(
    _members = Seq(Input, WConsumer, WProvider, Output, TConsumer),
    _bindings = Seq(
      Binding(Input,     "worldConsumer", WConsumer),
      Binding(WConsumer, "provider",      WProvider),
      Binding(WProvider, "target",        Output),
      Binding(Output,    "consumer",      TConsumer)
    ),
    _entry = Input
  )

  private type J = draco.format.json.Json
  private val constructors: Map[String, AssemblySpawner.Constructor] = Map(
    Input.namePath ->
      ((refs: Seq[ActorRef[Any]]) =>
        domains.aerial.Input.actorType(refs.head.asInstanceOf[ActorRef[World]]).asInstanceOf[Behavior[Any]]),
    WConsumer.namePath ->
      ((refs: Seq[ActorRef[Any]]) =>
        domains.world.Consumer.actorType(refs.head.asInstanceOf[ActorRef[World]]).asInstanceOf[Behavior[Any]]),
    WProvider.namePath ->
      ((refs: Seq[ActorRef[Any]]) =>
        domains.world.Provider.actorType(refs.head.asInstanceOf[ActorRef[World]]).asInstanceOf[Behavior[Any]]),
    Output.namePath ->
      ((refs: Seq[ActorRef[Any]]) =>
        domains.terrestrial.Output.actorType(refs.head.asInstanceOf[ActorRef[J]]).asInstanceOf[Behavior[Any]]),
    TConsumer.namePath ->
      ((_: Seq[ActorRef[Any]]) =>
        domains.terrestrial.Consumer.actorType().asInstanceOf[Behavior[Any]])
  )

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

  test("the five-member, two-message-type World service validates without a Pekko context") {
    assert(AssemblyValidator.validate(worldService) == Seq.empty)
  }

  test("the validator catches a binding wired to the wrong message type") {
    // Output receives World, so binding Output's Json-typed `consumer` param to it is a type error.
    val crossed = Assembly(
      _members  = worldService.members,
      _bindings = Seq(Binding(Output, "consumer", WProvider)),
      _entry    = Input
    )
    assert(AssemblyValidator.validate(crossed).nonEmpty)
  }

  test("the spawner runs the World service and meaning is preserved at the Terrestrial sink") {
    domains.terrestrial.TerrestrialSink.clear()

    val guardian: Behavior[J] = Behaviors.setup { ctx =>
      val entry: ActorRef[J] = AssemblySpawner.spawn[J](worldService, constructors)(ctx)
      Behaviors.receiveMessage { msg =>
        entry ! msg
        Behaviors.same
      }
    }

    val system = ActorSystem(guardian, "worldAssembly")
    system ! positionReport("NX5500", latitude = 51.5, longitude = -0.12, altitudeFeet = 35000)
    Thread.sleep(300)
    system.terminate()

    val recorded = domains.terrestrial.TerrestrialSink.recorded
    assert(recorded.nonEmpty, "Terrestrial sink recorded nothing")

    val out = parser.parse(recorded.head).toOption.get.hcursor
    assert(math.abs(out.get[Double]("latitude").toOption.get  - 51.5)  < 1e-6)
    assert(math.abs(out.get[Double]("longitude").toOption.get - -0.12) < 1e-6)
    assert(out.get[Int]("elevationMetres").toOption.get == 10668)   // 35000 ft -> 10668 m
  }
}
