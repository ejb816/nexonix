
package domains.marine

import draco._
import domains._
import org.apache.pekko.actor.typed.ActorRef
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait OriginateReportRule

object OriginateReportRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("OriginateReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[OriginateReportRule] = Type[OriginateReportRule] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  // Creation phase: originate a FixReport algorithmically from the voyage intent.
  // Marine's native representation diverges from the other media — discriminator
  // "kind", id "vessel", depth in fathoms not feet/metres, and the report concept
  // is a "FIX" (a determination one takes), distinct from position/location. The
  // origination is a clean unit transform (metres -> fathoms).
  private def originate(intent: VoyageIntent): FixReport = {
    val cur          = intent.value.hcursor
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
      override val value: io.circe.Json = payload
    }
  }

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val intent: VoyageIntent = ctx.get[VoyageIntent]("$intent")
      val consumer: ActorRef[draco.format.json.Json] =
        ctx.getRuntime().get[ActorRef[draco.format.json.Json]]("consumer")
      consumer ! originate(intent)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("domains.marine.OriginateReport.rule")
    .forEach (
      "$intent", classOf[VoyageIntent]
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
