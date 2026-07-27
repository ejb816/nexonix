package draco

import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait CollectProblems

object CollectProblems extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CollectProblems", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[CollectProblems] = Type[CollectProblems] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val p: Problem = ctx.get[Problem]("$p")
      ctx.getRuntime().get[java.util.List[Problem]]("problems").add(p)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.CollectProblems")
    .forEach (
      "$p", classOf[Problem]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[CollectProblems] (
    _pattern = pattern,
    _action = action
  )
}
