package domains.world

import draco._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/** World.Consumer — the transform INPUT face (Vishnu-at-work). Receives a typed
  * source value (a World subtype, decoded by a subdomain input adapter) and
  * produces the target representation through the change of form that PRESERVES
  * MEANING: it projects through the `Observable` world-fact (geodetic -> ECEF ->
  * geodetic), then hands the typed result to the `Provider`.
  *
  * This is where "all transform rules live in World" is realised — so World knows
  * the media types by design. For this first slice the transform is Scala (the
  * `Geodesy`/`Observable` core proven by `AerialTerrestrialTransformTest`);
  * expressing it as JSON-backed World rules is the next precursor to TransformBuilder. */
trait Consumer extends Actor[World]

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  def actorType(provider: ActorRef[World]): ActorType = new Actor[World] {
    override lazy val actorDefinition: TypeDefinition = Consumer.typeDefinition
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition

    override def receive(ctx: TypedActorContext[World], msg: World): Behavior[World] = {
      msg match {
        case position: domains.aerial.Position =>
          // input adapter projected nothing yet — World owns the geodesy: feet ->
          // metres, geodetic -> ECEF (the Observable world-fact) -> geodetic.
          val heightMetres  = position.altitudeFeet * 0.3048
          val observable    = Observable.fromGeodetic(position.latitude, position.longitude, heightMetres)
          val (lat, lon, h) = Observable.toGeodetic(observable)
          val location = domains.terrestrial.Location(
            _latitude        = lat,
            _longitude       = lon,
            _elevationMetres = math.round(h).toInt
          )
          provider ! location
        case _ => // no transform rule for this source type yet
      }
      Behaviors.same[World]
    }

    override def receiveSignal(ctx: TypedActorContext[World], signal: Signal): Behavior[World] = {
      Behaviors.same[World]
    }
  }
}
