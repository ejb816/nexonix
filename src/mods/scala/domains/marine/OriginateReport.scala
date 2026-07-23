
package domains.marine

import draco._
import domains._
import org.apache.pekko.actor.typed.ActorRef
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait OriginateReport

object OriginateReport extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[OriginateReport] = Type[OriginateReport] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  private def originate(intent: VoyageIntent): FixReport = {
    val cur          = intent.json.hcursor
    val vessel       = cur.get[String]("vessel").getOrElse("UNKNOWN")
    val depthMetres  = cur.get[Int]("depthMetres").getOrElse(0)
    val depthFathoms = (depthMetres * 10000) / 18288   // metres -> fathoms, integer
    val payload = io.circe.Json.obj(
      "kind" -> io.circe.Json.fromString("FIX"),
      "vessel" -> io.circe.Json.fromString(vessel),
      "depthFathoms" -> io.circe.Json.fromInt(depthFathoms)
    )
    new FixReport {
      override lazy val typeDefinition: TypeDefinition = FixReport.typeDefinition
      override val json: io.circe.Json = payload
    }
  }

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val intent: VoyageIntent = ctx.get[VoyageIntent]("$intent")
      val consumer: ActorRef[draco.format.json.JSON] =
        ctx.getRuntime().get[ActorRef[draco.format.json.JSON]]("consumer")
      consumer ! originate(intent)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.marine.OriginateReport")
    .forEach (
      "$intent", classOf[VoyageIntent]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[OriginateReport] (
    _pattern = pattern,
    _action = action
  )
}
