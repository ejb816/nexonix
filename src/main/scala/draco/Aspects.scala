package draco

trait Aspects {
  val dracoAspect: DracoAspect
  val domainAspect: DomainAspect
  val ruleAspect: RuleAspect
  val actorAspect: ActorAspect
}
