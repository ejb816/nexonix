package domains.world

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Consumer extends Actor[domains.world.World]

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = Rule.knowledgeService.newKnowledge("Consumer")

  def actorType(_provider: => domains.world.World => Unit): ActorType = new Actor[World] {
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition
    lazy val provider: domains.world.World => Unit = _provider

    override def receive(ctx: TypedActorContext[World], msg: World): Behavior[World] = {
      msg match {
        case position: domains.aerial.Position =>
          val heightMetres: Double = position.altitudeFeet * 0.3048
          val observable: Observable = Observable.fromGeodetic(position.latitude, position.longitude, heightMetres)
          val coordinates: (Double, Double, Double) = Observable.toGeodetic(observable)
          val location: domains.terrestrial.Location = domains.terrestrial.Location(coordinates._1, coordinates._2, math.round(coordinates._3).toInt)
          provider(location)
        case _ => ()
      }
      Behaviors.same[World]
    }

    override def receiveSignal(ctx: TypedActorContext[World], signal: Signal): Behavior[World] = {
      Behaviors.same[World]
    }
  }
}
