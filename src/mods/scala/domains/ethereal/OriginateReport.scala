package domains.ethereal

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}

trait OriginateReport

object OriginateReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[OriginateReport] = Type[OriginateReport] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val intent: LaunchIntent = ctx.get[LaunchIntent]("$intent")
      lazy val consumer: draco.format.dracon.Dracon => Unit = ctx.getRuntime().get[draco.format.dracon.Dracon => Unit]("consumer")
      lazy val cursor: io.circe.HCursor = intent.json.hcursor
      lazy val obj: String = cursor.get[String]("object").getOrElse("UNKNOWN")
      lazy val altitudeNauticalMiles: Int = cursor.get[Int]("altitudeNauticalMiles").getOrElse(0)
      lazy val altitudeKilometres: Int = altitudeNauticalMiles * 1852 / 1000
      lazy val payload: io.circe.Json = io.circe.Json.obj(("category", io.circe.Json.fromString("EPHEMERIS")), ("object", io.circe.Json.fromString(obj)), ("altitudeKilometres", io.circe.Json.fromInt(altitudeKilometres)))
      lazy val report: EphemerisReport = EphemerisReport(payload)
      consumer(report)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.ethereal.OriginateReport")
    .forEach (
      "$intent", classOf[LaunchIntent]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReport] (
    _pattern = pattern,
    _action = action
  )
}
