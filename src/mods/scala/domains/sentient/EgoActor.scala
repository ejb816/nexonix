package domains.sentient

import draco._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.slf4j.LoggerFactory

trait EgoActor

object EgoActor extends App {

  private lazy val log = LoggerFactory.getLogger(getClass)

  lazy val typeDefinition: TypeDefinition = Ego.typeDefinition
  def actorType(): ActorType = new Actor[Ego] {
    override val actorDefinition: TypeDefinition = Ego.typeDefinition
    override val typeDefinition: TypeDefinition = Ego.typeDefinition

    override def receive(ctx: TypedActorContext[Ego], msg: Ego): Behavior[Ego] = {
      log.debug(s"Ego received: $msg")
      Behaviors.same[Ego]
    }

    override def receiveSignal(ctx: TypedActorContext[Ego], msg: Signal): Behavior[Ego] = {
      Behaviors.same[Ego]
    }
  }
}
