
package domains.aerial

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait ConsumeReportRule

object ConsumeReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[ConsumeReportRule] = Type[ConsumeReportRule] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val report: PositionReport = ctx.get[PositionReport]("$report")
      ctx.getRuntime().get[java.util.List[String]]("consumed").add(report.json.noSpaces)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.aerial.ConsumeReport.rule")
    .forEach (
      "$report", classOf[PositionReport]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReportRule] (
    _pattern = pattern,
    _action = action
  )
}
