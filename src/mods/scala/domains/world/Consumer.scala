package domains.world

import draco._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait Consumer extends Actor[World]

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  def actorType(provider: ActorRef[World]): ActorType = new Actor[World] {
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition

    override def receive(ctx: TypedActorContext[World], msg: World): Behavior[World] = {
      msg match {
        case position: domains.aerial.Position =>
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
