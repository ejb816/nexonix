
package domains.aerial

import draco._
import domains._
import org.apache.pekko.actor.typed.ActorRef
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait OriginateReportRule

object OriginateReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[OriginateReportRule] = Type[OriginateReportRule] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  // Creation phase: the report is *originated algorithmically* from the intent —
  // no JSON fixture. Here the origination is a unit transform (flight level, in
  // hundreds of feet, → altitude in feet) carrying the callsign through. This is
  // the seam where a real medium would synthesize its native representation.
  private def originate(intent: FlightIntent): PositionReport = {
    val cur      = intent.value.hcursor
    val callsign = cur.get[String]("callsign").getOrElse("UNKNOWN")
    val fl       = cur.get[Int]("flightLevel").getOrElse(0)
    val payload  = io.circe.Json.obj(
      "message"      -> io.circe.Json.fromString("POSITION"),
      "callsign"     -> io.circe.Json.fromString(callsign),
      "altitudeFeet" -> io.circe.Json.fromInt(fl * 100)
    )
    new PositionReport {
      override lazy val typeDefinition: TypeDefinition = PositionReport.typeDefinition
      override val value: io.circe.Json = payload
    }
  }

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val intent: FlightIntent = ctx.get[FlightIntent]("$intent")
      val consumer: ActorRef[draco.format.json.Json] =
        ctx.getRuntime().get[ActorRef[draco.format.json.Json]]("consumer")
      consumer ! originate(intent)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.aerial.OriginateReport.rule")
    .forEach (
      "$intent", classOf[FlightIntent]
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
