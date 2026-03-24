package domains.dataModel

import domains.bravo._
import draco._
import io.circe.{Json, parser}
import org.evrete.api.{Knowledge, RhsContext}
import org.apache.pekko.actor.typed.ActorRef
import java.util.function.Consumer

trait AssembleResult extends RuleInstance

object AssembleResult extends App with RuleInstance {
  private lazy val ruleDefinition: TypeDefinition = parser.parse("""{
  "typeName" : {
    "name" : "AssembleResult",
    "namePackage" : ["domains", "dataModel"]
  },
  "variables" : [
    { "kind" : "Variable", "name" : "partOne", "valueType" : "PartOne" },
    { "kind" : "Variable", "name" : "partTwo", "valueType" : "PartTwo" }
  ],
  "action" : {
    "kind" : "Action",
    "valueType" : "Unit",
    "body" : [
      { "kind" : "Fixed", "name" : "result", "valueType" : "BravoResult", "value" : "Bravo.result(partOne.number, partTwo.text)" },
      { "kind" : "Monadic", "valueType" : "Unit", "value" : "bravoRef ! result" },
      { "kind" : "Monadic", "valueType" : "Unit", "value" : "ctx.delete(partOne)" },
      { "kind" : "Monadic", "valueType" : "Unit", "value" : "ctx.delete(partTwo)" }
    ]
  }
}""").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)

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

  lazy val ruleInstance: RuleType = Rule[AssembleResult](
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

  lazy val typeInstance: DracoType = Type[AssembleResult](typeDefinition)
}
