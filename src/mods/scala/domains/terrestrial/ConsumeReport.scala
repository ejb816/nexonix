
package domains.terrestrial

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}

trait ConsumeReport

object ConsumeReport extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ConsumeReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[ConsumeReport] = Type[ConsumeReport] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val report: LocationReport = ctx.get[LocationReport]("$report")
      domains.terrestrial.TerrestrialSink.record(report.json.noSpaces)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.terrestrial.ConsumeReport")
    .forEach (
      "$report", classOf[LocationReport]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[ConsumeReport] (
    _pattern = pattern,
    _action = action
  )
}
