package scenario.forest

import draco._
import scenario._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait BirchAlarmReceived

object BirchAlarmReceived extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("BirchAlarmReceived", _namePackage = Seq ("scenario", "forest")))
  lazy val dracoType: Type[BirchAlarmReceived] = Type[BirchAlarmReceived] (typeDefinition)
  lazy val domainType: Domain[Forest] = Domain[Forest] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val jasmonate: scenario.birch.BirchJasmonate = ctx.get[scenario.birch.BirchJasmonate]("$jasmonate")
      val received: java.util.List[scenario.birch.BirchJasmonate] = ctx.getRuntime().get("received")
      received.add(jasmonate)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("scenario.forest.BirchAlarmReceived")
    .forEach (
      "$jasmonate", classOf[scenario.birch.BirchJasmonate]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[BirchAlarmReceived] (
    _pattern = pattern,
    _action = action
  )
}
