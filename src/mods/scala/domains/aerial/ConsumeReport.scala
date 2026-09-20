package domains.aerial

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}
import scala.collection.mutable

trait ConsumeReport

object ConsumeReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[ConsumeReport] = Type[ConsumeReport] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val report: PositionReport = ctx.get[PositionReport]("$report")
      lazy val consumed: mutable.Buffer[String] = ctx.getRuntime().get("consumed")
      consumed += report.json.noSpaces
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.aerial.ConsumeReport")
    .forEach (
      "$report", classOf[PositionReport]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReport] (
    _pattern = pattern,
    _action = action
  )
}
