
package domains.marine

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}

trait ConsumeReport

object ConsumeReport extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[ConsumeReport] = Type[ConsumeReport] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val report: FixReport = ctx.get[FixReport]("$report")
      domains.marine.MarineSink.record(report.json.noSpaces)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.marine.ConsumeReport")
    .forEach (
      "$report", classOf[FixReport]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReport] (
    _pattern = pattern,
    _action = action
  )
}
