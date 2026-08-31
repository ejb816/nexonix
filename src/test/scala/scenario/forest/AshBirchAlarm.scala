package scenario.forest

import draco._
import scenario._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait AshBirchAlarm

object AshBirchAlarm extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AshBirchAlarm", _namePackage = Seq ("scenario", "forest")))
  lazy val dracoType: Type[AshBirchAlarm] = Type[AshBirchAlarm] (typeDefinition)
  lazy val domainType: Domain[Forest] = Domain[Forest] (typeDefinition)
  def w0(jasmonate: scenario.ash.AshJasmonate): Boolean = jasmonate.potency.value > 0.5
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val jasmonate: scenario.ash.AshJasmonate = ctx.get[scenario.ash.AshJasmonate]("$jasmonate")
      val primed: scenario.birch.BirchJasmonate = scenario.birch.BirchJasmonate(scenario.ash.birch.Potency(jasmonate.potency), scenario.ash.birch.Marker(jasmonate.compound))
      ctx.insert(primed)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("scenario.forest.AshBirchAlarm")
    .forEach (
      "$jasmonate", classOf[scenario.ash.AshJasmonate]
    )
    .where("scenario.forest.AshBirchAlarm.w0($jasmonate)")
    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[AshBirchAlarm] (
    _pattern = pattern,
    _action = action
  )
}
