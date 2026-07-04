
package domains.terrestrial

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait ConsumeReportRule

object ConsumeReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[ConsumeReportRule] = Type[ConsumeReportRule] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val report: LocationReport = ctx.get[LocationReport]("$report")
      domains.terrestrial.TerrestrialSink.record(report.value.noSpaces)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.terrestrial.ConsumeReport.rule")
    .forEach (
      "$report", classOf[LocationReport]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReportRule] (
    _pattern = pattern,
    _action = action
  )
}
