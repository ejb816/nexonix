package draco

import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait Completeness

object Completeness extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Completeness", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Completeness] = Type[Completeness] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(td: TypeDefinition): Boolean = draco.DracoAspect.isEmpty(td.dracoAspect)
  def w1(td: TypeDefinition): Boolean = draco.DomainAspect.isEmpty(td.domainAspect)
  def w2(td: TypeDefinition): Boolean = draco.RuleAspect.isEmpty(td.ruleAspect)
  def w3(td: TypeDefinition): Boolean = draco.ActorAspect.isEmpty(td.actorAspect)
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val td: TypeDefinition = ctx.get[TypeDefinition]("$td")
      ctx.insert(Problem(td.typeName, s"member ${td.typeName.name} is declared but unauthored (no JSON on disk)"))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.Completeness")
    .forEach (
      "$td", classOf[TypeDefinition]
    )
    .where("draco.Completeness.w0($td)")
    .where("draco.Completeness.w1($td)")
    .where("draco.Completeness.w2($td)")
    .where("draco.Completeness.w3($td)")
    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[Completeness] (
    _pattern = pattern,
    _action = action
  )
}
