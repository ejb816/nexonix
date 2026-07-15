
package domains.terrestrial

import draco._
import domains._
import org.apache.pekko.actor.typed.ActorRef
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait OriginateReportRule

object OriginateReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[OriginateReportRule] = Type[OriginateReportRule] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  private def originate(intent: MarchIntent): LocationReport = {
    val cur         = intent.json.hcursor
    val unit        = cur.get[String]("unit").getOrElse("UNKNOWN")
    val elevationFt = cur.get[Int]("elevationFeet").getOrElse(0)
    val elevationM  = (elevationFt * 3048) / 10000   // feet -> metres, integer
    val payload = io.circe.Json.obj(
      "type" -> io.circe.Json.fromString("LOCATION"),
      "unit" -> io.circe.Json.fromString(unit),
      "elevationMetres" -> io.circe.Json.fromInt(elevationM)
    )
    new LocationReport {
      override lazy val typeDefinition: TypeDefinition = LocationReport.typeDefinition
      override val json: io.circe.Json = payload
    }
  }

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val intent: MarchIntent = ctx.get[MarchIntent]("$intent")
      val consumer: ActorRef[draco.format.json.JSON] =
        ctx.getRuntime().get[ActorRef[draco.format.json.JSON]]("consumer")
      consumer ! originate(intent)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.terrestrial.OriginateReport.rule")
    .forEach (
      "$intent", classOf[MarchIntent]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReportRule] (
    _pattern = pattern,
    _action = action
  )
}
