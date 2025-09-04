package draco

import org.evrete.api.Knowledge

trait RuleActorBehavior[T] extends ActorBehavior[T] {
  val knowledge: Knowledge
}

object RuleActorBehavior {
  def apply[T](_knowledge: Knowledge): RuleActorBehavior[T] = new RuleActorBehavior[T] {
    override val knowledge: Knowledge = _knowledge
  }
}