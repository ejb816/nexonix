package draco.domain

import org.evrete.api.Knowledge

trait DomainRuleActor[T] extends DomainActor[T] {
  val knowledge: Knowledge
}

object DomainRuleActor {
  def apply[T](_knowledge: Knowledge): DomainRuleActor[T] = new DomainRuleActor[T] {
    override val knowledge: Knowledge = _knowledge
  }
}