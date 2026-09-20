package draco

import org.evrete.api.{Knowledge, RhsContext}
import scala.collection.mutable

trait CollectProblems

object CollectProblems extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CollectProblems", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[CollectProblems] = Type[CollectProblems] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val p: Problem = ctx.get[Problem]("$p")
      lazy val problems: mutable.Buffer[Problem] = ctx.getRuntime().get("problems")
      problems += p
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.CollectProblems")
    .forEach (
      "$p", classOf[Problem]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[CollectProblems] (
    _pattern = pattern,
    _action = action
  )
}
