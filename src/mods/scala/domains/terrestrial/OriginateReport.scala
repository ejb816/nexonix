package domains.terrestrial

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}

trait OriginateReport

object OriginateReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[OriginateReport] = Type[OriginateReport] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val intent: MarchIntent = ctx.get[MarchIntent]("$intent")
      lazy val consumer: draco.format.dracon.Dracon => Unit = ctx.getRuntime().get[draco.format.dracon.Dracon => Unit]("consumer")
      lazy val cursor: io.circe.HCursor = intent.json.hcursor
      lazy val unit: String = cursor.get[String]("unit").getOrElse("UNKNOWN")
      lazy val elevationFeet: Int = cursor.get[Int]("elevationFeet").getOrElse(0)
      lazy val elevationMetres: Int = elevationFeet * 3048 / 10000
      lazy val payload: io.circe.Json = io.circe.Json.obj(("type", io.circe.Json.fromString("LOCATION")), ("unit", io.circe.Json.fromString(unit)), ("elevationMetres", io.circe.Json.fromInt(elevationMetres)))
      lazy val report: LocationReport = LocationReport(payload)
      consumer(report)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.terrestrial.OriginateReport")
    .forEach (
      "$intent", classOf[MarchIntent]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReport] (
    _pattern = pattern,
    _action = action
  )
}
