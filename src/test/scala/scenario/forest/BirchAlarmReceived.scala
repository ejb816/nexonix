package scenario.forest

import draco._
import scenario._
import org.evrete.api.{Knowledge, RhsContext}
import scala.collection.mutable

trait BirchAlarmReceived

object BirchAlarmReceived extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("BirchAlarmReceived", _namePackage = Seq ("scenario", "forest")))
  lazy val dracoType: Type[BirchAlarmReceived] = Type[BirchAlarmReceived] (typeDefinition)
  lazy val domainType: Domain[Forest] = Domain[Forest] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val jasmonate: scenario.birch.BirchJasmonate = ctx.get[scenario.birch.BirchJasmonate]("$jasmonate")
      lazy val received: mutable.Buffer[scenario.birch.BirchJasmonate] = ctx.getRuntime().get("received")
      received += jasmonate
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("scenario.forest.BirchAlarmReceived")
    .forEach (
      "$jasmonate", classOf[scenario.birch.BirchJasmonate]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[BirchAlarmReceived] (
    _pattern = pattern,
    _action = action
  )
}
