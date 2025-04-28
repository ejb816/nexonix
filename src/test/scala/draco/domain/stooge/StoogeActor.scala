package draco.domain.stooge

import draco.domain.actor.DomainActor

trait StoogeActor extends DomainActor  {
  lazy val name: String = this.getClass.getSimpleName
  val behavior: Behavior[StoogeActor] = new Behavior[StoogeActor](1) {

  }

  def receive: Receive = {
    case msg =>
      val msgFrom = sender()
      println(s"${name}: Received message: $msg from $msgFrom");
  }
}

object StoogeActor {
  def apply() : StoogeActor = new StoogeActor {}
  trait Action
  trait Slap extends Action
  object Slap { def apply(): Slap = new Slap {} }
  trait Bonk extends Action
  object Bonk { def apply(): Bonk = new Bonk {} }
  trait Poke extends Action
  object Poke { def apply(): Poke = new Poke {} }
}
