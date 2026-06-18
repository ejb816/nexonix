
package domains.ethereal

import draco._
import domains._
import org.apache.pekko.actor.typed.ActorRef
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait OriginateReportRule

object OriginateReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[OriginateReportRule] = Type[OriginateReportRule] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)

  // Creation phase: originate an EphemerisReport algorithmically from the launch
  // intent. Ethereal's native representation diverges from the other media —
  // discriminator "category", id "object", altitude in kilometres, and the report
  // concept is an "EPHEMERIS" (a position computed from orbital mechanics), distinct
  // from position/location/fix. The origination is a clean unit transform (nautical
  // miles -> kilometres).
  private def originate(intent: LaunchIntent): EphemerisReport = {
    val cur                 = intent.value.hcursor
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
      override val value: io.circe.Json = payload
    }
  }

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val intent: LaunchIntent = ctx.get[LaunchIntent]("$intent")
      val consumer: ActorRef[draco.format.json.Json] =
        ctx.getRuntime().get[ActorRef[draco.format.json.Json]]("consumer")
      consumer ! originate(intent)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.ethereal.OriginateReport.rule")
    .forEach (
      "$intent", classOf[LaunchIntent]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReportRule] (
    typeDefinition,
    _pattern = pattern,
    _action = action
  )
}
