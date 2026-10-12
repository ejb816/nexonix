package draco

import org.evrete.api.{Knowledge, RhsContext}

trait Uniqueness

object Uniqueness extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Uniqueness", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Uniqueness] = Type[Uniqueness] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(m: TypeDefinition, o: DomainOntology): Boolean = o.keys.count(domain => o.owns(domain, m.typeName)) > 1
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val m: TypeDefinition = ctx.get[TypeDefinition]("$m")
      val o: DomainOntology = ctx.get[DomainOntology]("$o")
      ctx.insert(Problem(m.typeName, s"name ${m.typeName.namePath} is owned by more than one domain"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.Uniqueness")
    .forEach (
      "$m", classOf[TypeDefinition],
      "$o", classOf[DomainOntology]
    )
    .where("draco.Uniqueness.w0($m, $o)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[Uniqueness] (
    _pattern = pattern,
    _action = action
  )
}
