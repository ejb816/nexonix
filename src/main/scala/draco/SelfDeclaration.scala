package draco

import org.evrete.api.{Knowledge, RhsContext}

trait SelfDeclaration

object SelfDeclaration extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("SelfDeclaration", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[SelfDeclaration] = Type[SelfDeclaration] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(d: DomainType): Boolean = d.typeDefinition.domainAspect.typeName.name != d.typeDefinition.typeName.name || d.typeDefinition.domainAspect.typeName.namePackage != d.typeDefinition.typeName.namePackage
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val d: DomainType = ctx.get[DomainType]("$d")
      ctx.insert(Problem(d.typeDefinition.typeName, s"domain ${d.typeDefinition.typeName.name} does not self-declare: domainAspect.typeName is ${d.typeDefinition.domainAspect.typeName.name}"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.SelfDeclaration")
    .forEach (
      "$d", classOf[DomainType]
    )
    .where("draco.SelfDeclaration.w0($d)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[SelfDeclaration] (
    _pattern = pattern,
    _action = action
  )
}
