package draco.domain.rule

import draco.domain.actor.DomainActor
import org.evrete.api.Knowledge

trait DomainRuleActor extends DomainActor {
  val knowledge: Knowledge
}

object DomainRuleActor {
  def apply(_knowledge: Knowledge): DomainRuleActor = new DomainRuleActor {
    override val knowledge: Knowledge = _knowledge

    override def receive: Receive = ???
  }
}