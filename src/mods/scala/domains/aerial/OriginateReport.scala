package domains.aerial

import draco.format.dracon.Dracon

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}

trait OriginateReport

object OriginateReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[OriginateReport] = Type[OriginateReport] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val intent: FlightIntent = ctx.get[FlightIntent]("$intent")
      lazy val consumer: Dracon => Unit = ctx.getRuntime().get[Dracon => Unit]("consumer")
      lazy val cursor: io.circe.HCursor = intent.json.hcursor
      lazy val callsign: String = cursor.get[String]("callsign").getOrElse("UNKNOWN")
      lazy val flightLevel: Int = cursor.get[Int]("flightLevel").getOrElse(0)
      lazy val payload: io.circe.Json = io.circe.Json.obj(("message", io.circe.Json.fromString("POSITION")), ("callsign", io.circe.Json.fromString(callsign)), ("altitudeFeet", io.circe.Json.fromInt(flightLevel * 100)))
      lazy val report: PositionReport = PositionReport(payload)
      consumer(report)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.aerial.OriginateReport")
    .forEach (
      "$intent", classOf[FlightIntent]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReport] (
    _pattern = pattern,
    _action = action
  )
}
