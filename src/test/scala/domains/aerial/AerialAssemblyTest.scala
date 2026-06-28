package domains.aerial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

/** The Aerial Creator -> Consumer chain expressed as a draco [[Assembly]] rather
  * than a hand-written guardian.
  *
  * The wiring is pure data: two members and one binding saying "Creator's
  * `consumer` construction parameter is filled by the Consumer member". Two
  * properties are exercised:
  *   1. the assembly validates with no Pekko in sight ([[AssemblyValidator]]);
  *   2. [[AssemblySpawner]] turns that same data into the running actor group,
  *      threading the spawned Consumer ref into Creator, and the end-to-end
  *      behaviour matches the hand-wired version.
  */
class AerialAssemblyTest extends AnyFunSuite {

  private val aerialChain: Assembly = Assembly(
    _members = Seq(Consumer.typeDefinition.typeName, Creator.typeDefinition.typeName),
    _bindings = Seq(
      Binding(Creator.typeDefinition.typeName, "consumer", Consumer.typeDefinition.typeName)
    ),
    _entry = Creator.typeDefinition.typeName
  )

  private val constructors: Map[String, AssemblySpawner.Constructor] = Map(
    Consumer.typeDefinition.typeName.namePath ->
      ((_: Seq[ActorRef[Any]]) => Consumer.actorType().asInstanceOf[Behavior[Any]]),
    Creator.typeDefinition.typeName.namePath ->
      ((refs: Seq[ActorRef[Any]]) =>
        Creator.actorType(refs.head.asInstanceOf[ActorRef[draco.format.json.Json]]).asInstanceOf[Behavior[Any]])
  )

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

  test("the Aerial chain assembly validates without a Pekko context") {
    assert(AssemblyValidator.validate(aerialChain) == Seq.empty)
  }

  test("a mis-wired assembly is reported by the validator") {
    val broken = Assembly(
      _members  = aerialChain.members,
      _bindings = Seq(Binding(Creator.typeDefinition.typeName, "noSuchParam", Consumer.typeDefinition.typeName)),
      _entry    = Creator.typeDefinition.typeName
    )
    assert(AssemblyValidator.validate(broken).nonEmpty)
  }

  test("the spawner runs the assembly and the stateful Consumer reaps at PostStop") {
    AerialSink.clear()

    val guardian: Behavior[draco.format.json.Json] = Behaviors.setup { ctx =>
      val entry: ActorRef[draco.format.json.Json] =
        AssemblySpawner.spawn[draco.format.json.Json](aerialChain, constructors)(ctx)
      Behaviors.receiveMessage { msg =>
        entry ! msg
        Behaviors.same
      }
    }

    val system = ActorSystem(guardian, "aerialAssembly")
    system ! intent("NX5500", 390)   // FL390 -> 39000 ft
    system ! intent("NX6600", 350)   // FL350 -> 35000 ft
    Thread.sleep(300)
    system.terminate()
    Await.result(system.whenTerminated, 3.seconds)

    val recorded = AerialSink.recorded
    assert(recorded.exists(_.contains("NX5500")))
    assert(recorded.exists(_.contains("39000")))
    assert(recorded.exists(_.contains("NX6600")))
    assert(recorded.exists(_.contains("35000")))
  }
}
