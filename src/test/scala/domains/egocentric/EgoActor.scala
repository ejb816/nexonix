package domains.egocentric

import draco._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}

trait EgoActor

object EgoActor extends App {

  lazy val typeDefinition: TypeDefinition = Ego.typeDefinition
  lazy val actorType: ActorType = new Actor[Ego] {
    override val actorDefinition: TypeDefinition = Ego.typeDefinition
    override val typeDefinition: TypeDefinition = Ego.typeDefinition

    override def receive(ctx: TypedActorContext[Ego], msg: Ego): Behavior[Ego] = {
      println(s"Ego received: $msg")
      Behaviors.same[Ego]
    }

    override def receiveSignal(ctx: TypedActorContext[Ego], msg: Signal): Behavior[Ego] = {
      Behaviors.same[Ego]
    }
  }
}
