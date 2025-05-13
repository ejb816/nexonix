package draco.domain.stooge

import draco.domain.actor.DomainActor
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait StoogeActor extends DomainActor  {
  lazy val name: String = this.getClass.getSimpleName
  val stoogeBehavior: Behavior[StoogeActor.Action] = Behaviors.receive { (context, message) =>
    message match {
      case _: StoogeActor.Bonk =>
        println(s"$name bonked!")
      case _: StoogeActor.Poke =>
        println(s"$name poked!")
      case _: StoogeActor.Poke =>
        println(s"$name slapped!")
    }
    Behaviors.same
  }
}

object StoogeActor extends App {
  val systemBehavior: Behavior[Action] = Behaviors.receive { (context, message) =>
    Behaviors.same
  }
  def apply() : StoogeActor = new StoogeActor {}
  trait Action
  trait Slap extends Action
  object Slap { def apply(): Slap = new Slap {} }
  trait Bonk extends Action
  object Bonk { def apply(): Bonk = new Bonk {} }
  trait Poke extends Action
  object Poke { def apply(): Poke = new Poke {} }
}
