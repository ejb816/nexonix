
package domains.marine

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait ConsumeReportRule

object ConsumeReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[ConsumeReportRule] = Type[ConsumeReportRule] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val report: FixReport = ctx.get[FixReport]("$report")
      domains.marine.MarineSink.record(report.value.noSpaces)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.marine.ConsumeReport.rule")
    .forEach (
      "$report", classOf[FixReport]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReportRule] (
    _pattern = pattern,
    _action = action
  )
}
