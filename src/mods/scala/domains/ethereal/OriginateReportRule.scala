
package domains.ethereal

import draco._
import domains._
import org.apache.pekko.actor.typed.ActorRef
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait OriginateReportRule

object OriginateReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[OriginateReportRule] = Type[OriginateReportRule] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)

  private def originate(intent: LaunchIntent): EphemerisReport = {
    val cur                 = intent.json.hcursor
    val obj                 = cur.get[String]("object").getOrElse("UNKNOWN")
    val altitudeNauticalMi  = cur.get[Int]("altitudeNauticalMiles").getOrElse(0)
    val altitudeKm          = (altitudeNauticalMi * 1852) / 1000   // nautical miles -> kilometres, integer
    val payload = io.circe.Json.obj(
      "category" -> io.circe.Json.fromString("EPHEMERIS"),
      "object" -> io.circe.Json.fromString(obj),
      "altitudeKilometres" -> io.circe.Json.fromInt(altitudeKm)
    )
    new EphemerisReport {
      override lazy val typeDefinition: TypeDefinition = EphemerisReport.typeDefinition
      override val json: io.circe.Json = payload
    }
  }

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val intent: LaunchIntent = ctx.get[LaunchIntent]("$intent")
      val consumer: ActorRef[draco.format.json.JSON] =
        ctx.getRuntime().get[ActorRef[draco.format.json.JSON]]("consumer")
      consumer ! originate(intent)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.ethereal.OriginateReport")
    .forEach (
      "$intent", classOf[LaunchIntent]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReportRule] (
    _pattern = pattern,
    _action = action
  )
}
