package draco

import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait DerivationResolvable

object DerivationResolvable extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DerivationResolvable", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DerivationResolvable] = Type[DerivationResolvable] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(m: TypeDefinition): Boolean = m.dracoAspect.derivation.exists(anc => anc.namePackage.headOption.contains("draco") && { val a = draco.TypeLoader.loadType(anc); draco.DracoAspect.isEmpty(a.dracoAspect) && draco.DomainAspect.isEmpty(a.domainAspect) && draco.RuleAspect.isEmpty(a.ruleAspect) && draco.ActorAspect.isEmpty(a.actorAspect) })
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val m: TypeDefinition = ctx.get[TypeDefinition]("$m")
      ctx.insert(Problem(m.typeName, s"member ${m.typeName.name} derives from a draco type that does not resolve to a definition"))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.DerivationResolvable")
    .forEach (
      "$m", classOf[TypeDefinition]
    )
    .where("draco.DerivationResolvable.w0($m)")
    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[DerivationResolvable] (
    _pattern = pattern,
    _action = action
  )
}
