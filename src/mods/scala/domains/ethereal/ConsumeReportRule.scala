
package domains.ethereal

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait ConsumeReportRule

object ConsumeReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[ConsumeReportRule] = Type[ConsumeReportRule] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val report: EphemerisReport = ctx.get[EphemerisReport]("$report")
      domains.ethereal.EtherealSink.record(report.value.noSpaces)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.ethereal.ConsumeReport.rule")
    .forEach (
      "$report", classOf[EphemerisReport]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReportRule] (
    _pattern = pattern,
    _action = action
  )
}
