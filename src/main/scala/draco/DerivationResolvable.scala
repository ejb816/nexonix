package draco

import org.evrete.api.{Knowledge, RhsContext}

trait DerivationResolvable

object DerivationResolvable extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DerivationResolvable", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DerivationResolvable] = Type[DerivationResolvable] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(m: TypeDefinition, o: DomainOntology): Boolean = o.derivations.exists(edge => edge.child == m.typeName && !o.defines(edge.parent))
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val m: TypeDefinition = ctx.get[TypeDefinition]("$m")
      val o: DomainOntology = ctx.get[DomainOntology]("$o")
      ctx.insert(Problem(m.typeName, s"member ${m.typeName.name} derives from a type the ontology does not define"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.DerivationResolvable")
    .forEach (
      "$m", classOf[TypeDefinition],
      "$o", classOf[DomainOntology]
    )
    .where("draco.DerivationResolvable.w0($m, $o)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[DerivationResolvable] (
    _pattern = pattern,
    _action = action
  )
}
