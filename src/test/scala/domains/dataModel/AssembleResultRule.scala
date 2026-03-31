package domains.dataModel

import domains.bravo._
import draco._
import org.evrete.api.{Knowledge, RhsContext}
import org.apache.pekko.actor.typed.ActorRef
import java.util.function.Consumer

trait AssembleResultRule extends RuleInstance

object AssembleResultRule extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = draco.Generator.loadRuleType(TypeName ("AssembleResult", _namePackage = Seq("domains", "dataModel")))

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
    val partOne: PartOne = ctx.get[PartOne]("$partOne")
    val partTwo: PartTwo = ctx.get[PartTwo]("$partTwo")
    val result: BravoResult = Bravo.result(partOne.number, partTwo.text)
    val bravoRef: ActorRef[Bravo] = ctx.getRuntime.get[ActorRef[Bravo]]("bravoActorRef")
    bravoRef ! result
    ctx.delete(partOne)
    ctx.delete(partTwo)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
      .builder()
      .newRule("domains.dataModel.AssembleResult")
      .forEach(
        "$partOne", classOf[PartOne],
        "$partTwo", classOf[PartTwo]
      )
      .execute(action)
      .build()
  }

  lazy val ruleInstance: RuleType = Rule[AssembleResultRule](
    ruleDefinition,
    _pattern = pattern,
    _action = action
  )

  lazy val typeDefinition: TypeDefinition = TypeDefinition(
    _typeName = ruleDefinition.typeName,
    _derivation = Seq(
      RuleInstance.typeInstance.typeDefinition.typeName
    )
  )

  lazy val typeInstance: DracoType = Type[AssembleResultRule](typeDefinition)
}
