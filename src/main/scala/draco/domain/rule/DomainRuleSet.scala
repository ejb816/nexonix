package draco.domain.rule

import org.evrete.api.Knowledge

trait DomainRuleSet {
  val knowledge: Knowledge
  val domainRules: Seq[DomainRule]
  def load () : Unit = domainRules.foreach(dr => dr(knowledge))
}
object DomainRuleSet {
  def apply (
              _knowledge: Knowledge,
              _domainRules: Seq[DomainRule]
            ) : DomainRuleSet = new DomainRuleSet {
    override val knowledge: Knowledge = _knowledge
    override val domainRules: Seq[DomainRule] = _domainRules
  }
}
