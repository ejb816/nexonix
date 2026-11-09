package draco

import org.evrete.api.{Knowledge, RhsContext}

trait Completeness extends DracoType

object Completeness extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Completeness", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Completeness] = Type[Completeness] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
  def w0(td: TypeDefinition): Boolean = TypeDefinition.isStub(td)
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val td: TypeDefinition = ctx.get[TypeDefinition]("$td")
      ctx.insert(Problem(td.typeName, s"member ${td.typeName.name} is declared but unauthored (no JSON on disk)"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.Completeness")
    .forEach (
      "$td", classOf[TypeDefinition]
    )
    .where("draco.Completeness.w0($td)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[Completeness] (
    _pattern = pattern,
    _action = action
  )
}
