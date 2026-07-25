package draco

import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait SelfDeclaration

object SelfDeclaration extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("SelfDeclaration", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[SelfDeclaration] = Type[SelfDeclaration] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(d: DomainType): Boolean = d.typeDefinition.domainAspect.typeName.name != d.typeDefinition.typeName.name || d.typeDefinition.domainAspect.typeName.namePackage != d.typeDefinition.typeName.namePackage
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val d: DomainType = ctx.get[DomainType]("$d")
      ctx.insert(Problem(d.typeDefinition.typeName, s"domain ${d.typeDefinition.typeName.name} does not self-declare: domainAspect.typeName is ${d.typeDefinition.domainAspect.typeName.name}"))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.SelfDeclaration")
    .forEach (
      "$d", classOf[DomainType]
    )
    .where("draco.SelfDeclaration.w0($d)")
    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[SelfDeclaration] (
    _pattern = pattern,
    _action = action
  )
}
