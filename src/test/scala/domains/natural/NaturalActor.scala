package domains.natural

import draco._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}

trait NaturalActor

object NaturalActor extends App {

  lazy val typeDefinition: TypeDefinition = Natural.typeDefinition
  lazy val actorType: ActorType = new Actor[Natural] {
    override val actorDefinition: TypeDefinition = Natural.typeDefinition
    override val typeDefinition: TypeDefinition = Natural.typeDefinition

    override def receive(ctx: TypedActorContext[Natural], msg: Natural): Behavior[Natural] = {
      println(s"msg.value = ${msg.value}")
      Behaviors.same[Natural]
    }

    override def receiveSignal(ctx: TypedActorContext[Natural], msg: Signal): Behavior[Natural] = {
      Behaviors.same[Natural]
    }
  }
}
