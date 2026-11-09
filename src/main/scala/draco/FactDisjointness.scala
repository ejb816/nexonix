package draco

import org.evrete.api.{Knowledge, RhsContext}

trait FactDisjointness extends DracoType

object FactDisjointness extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FactDisjointness", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[FactDisjointness] = Type[FactDisjointness] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(d: DomainType, o: DomainOntology): Boolean = o.relatedFactTypes(d).nonEmpty
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val d: DomainType = ctx.get[DomainType]("$d")
      val o: DomainOntology = ctx.get[DomainOntology]("$o")
      lazy val pairs: Seq[String] = o.relatedFactTypes(d).map(pair => pair._1.namePath ++ " derives " ++ pair._2.namePath)
      ctx.insert(Problem(d.typeDefinition.typeName, s"rules of ${d.typeDefinition.typeName.namePath} declare fact types related by derivation: ${pairs.mkString(", ")}"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.FactDisjointness")
    .forEach (
      "$d", classOf[DomainType],
      "$o", classOf[DomainOntology]
    )
    .where("draco.FactDisjointness.w0($d, $o)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[FactDisjointness] (
    _pattern = pattern,
    _action = action
  )
}
