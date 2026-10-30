package domains.marine

import draco._
import domains._
import org.evrete.api.{Knowledge, RhsContext}

trait OriginateReport

object OriginateReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[OriginateReport] = Type[OriginateReport] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val intent: VoyageIntent = ctx.get[VoyageIntent]("$intent")
      lazy val consumer: draco.format.dracon.Dracon => Unit = ctx.getRuntime().get[draco.format.dracon.Dracon => Unit]("consumer")
      lazy val cursor: io.circe.HCursor = intent.json.hcursor
      lazy val vessel: String = cursor.get[String]("vessel").getOrElse("UNKNOWN")
      lazy val depthMetres: Int = cursor.get[Int]("depthMetres").getOrElse(0)
      lazy val depthFathoms: Int = depthMetres * 10000 / 18288
      lazy val payload: io.circe.Json = io.circe.Json.obj(("kind", io.circe.Json.fromString("FIX")), ("vessel", io.circe.Json.fromString(vessel)), ("depthFathoms", io.circe.Json.fromInt(depthFathoms)))
      lazy val report: FixReport = FixReport(payload)
      consumer(report)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.marine.OriginateReport")
    .forEach (
      "$intent", classOf[VoyageIntent]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReport] (
    _pattern = pattern,
    _action = action
  )
}
