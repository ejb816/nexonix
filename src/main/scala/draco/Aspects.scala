package draco

trait Aspects extends DracoType {
  val dracoAspect: DracoAspect
  val domainAspect: DomainAspect
  val ruleAspect: RuleAspect
  val actorAspect: ActorAspect
  val codecAspect: CodecAspect
}

object Aspects extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Aspects", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Aspects] = Type[Aspects] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
