package domains.aerial

import draco._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/** Aerial input adapter (codec, World-bound): decodes a loose `PositionReport` Json
  * into the typed `Position` — a direct subtype of `Aerial`, hence an indirect
  * subtype of `World` — and hands it to `World.Consumer`. The only place that knows
  * Aerial's wire schema; World itself deals only in typed values.
  *
  * Definition-backed (`Input.json`, `actorAspect.messageAction`) like the medium's
  * `Creator`/`Consumer`; the Scala body stays hand-written until the actor-emission
  * Generator fold. Not a `domainAspect` member of Aerial — actors are defined types
  * but not message-type members, matching the `Creator`/`Consumer` convention. */
trait Input extends Actor[draco.format.json.Json]

object Input extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Input", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Input] = Type[Input] (typeDefinition)

  def actorType(worldConsumer: ActorRef[domains.world.World]): ActorType = new Actor[draco.format.json.Json] {
    override lazy val actorDefinition: TypeDefinition = Input.typeDefinition
    override lazy val typeDefinition: TypeDefinition = Input.typeDefinition

    override def receive(ctx: TypedActorContext[draco.format.json.Json], msg: draco.format.json.Json): Behavior[draco.format.json.Json] = {
      val cursor   = msg.value.hcursor
      val position = Position(
        _latitude     = cursor.get[Double]("latitude").getOrElse(0.0),
        _longitude    = cursor.get[Double]("longitude").getOrElse(0.0),
        _altitudeFeet = cursor.get[Int]("altitudeFeet").getOrElse(0)
      )
      worldConsumer ! position
      Behaviors.same[draco.format.json.Json]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.json.Json], signal: Signal): Behavior[draco.format.json.Json] = {
      Behaviors.same[draco.format.json.Json]
    }
  }
}
