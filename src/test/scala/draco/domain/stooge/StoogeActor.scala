package draco.domain.stooge

import draco.domain.DomainActor

trait StoogeActor extends DomainActor[StoogeAction]  {
  lazy val name: String = this.getClass.getSimpleName
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
