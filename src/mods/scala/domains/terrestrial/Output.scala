package domains.terrestrial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/** Terrestrial output adapter (codec): encodes a typed `Location` (a World subtype)
  * back into a loose `LocationReport` Json and hands it to the medium's `Consumer`
  * — the sink face of Terrestrial's SourceSink. The only place that knows
  * Terrestrial's wire schema.
  *
  * Definition-backed (`Output.json`, `actorAspect.messageAction`) like the medium's
  * `Creator`/`Consumer`; the Scala body stays hand-written until the actor-emission
  * Generator fold. Not a `domainAspect` member of Terrestrial — actors are defined
  * types but not message-type members, matching the `Creator`/`Consumer` convention. */
trait Output extends Actor[domains.world.World]

object Output extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Output", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Output] = Type[Output] (typeDefinition)

  def actorType(consumer: ActorRef[draco.format.json.Json]): ActorType = new Actor[domains.world.World] {
    override lazy val actorDefinition: TypeDefinition = Output.typeDefinition
    override lazy val typeDefinition: TypeDefinition = Output.typeDefinition

    override def receive(ctx: TypedActorContext[domains.world.World], msg: domains.world.World): Behavior[domains.world.World] = {
      msg match {
        case location: Location =>
          val payload = Json.obj(
            "type"            -> Json.fromString("LOCATION"),
            "latitude"        -> Json.fromDoubleOrNull(location.latitude),
            "longitude"       -> Json.fromDoubleOrNull(location.longitude),
            "elevationMetres" -> Json.fromInt(location.elevationMetres)
          )
          val report = new LocationReport {
            override lazy val typeDefinition: TypeDefinition = LocationReport.typeDefinition
            override val value: Json = payload
          }
          consumer ! report
        case _ => // not a Location; nothing to encode
      }
      Behaviors.same[domains.world.World]
    }

    override def receiveSignal(ctx: TypedActorContext[domains.world.World], signal: Signal): Behavior[domains.world.World] = {
      Behaviors.same[domains.world.World]
    }
  }
}
