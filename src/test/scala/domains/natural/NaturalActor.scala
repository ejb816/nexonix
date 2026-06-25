package domains.natural

import draco._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.slf4j.LoggerFactory

trait NaturalActor

object NaturalActor extends App {

  private lazy val log = LoggerFactory.getLogger(getClass)

  lazy val typeDefinition: TypeDefinition = Natural.typeDefinition
  def actorType(): ActorType = new Actor[Natural] {
    override val actorDefinition: TypeDefinition = Natural.typeDefinition
    override val typeDefinition: TypeDefinition = Natural.typeDefinition

    override def receive(ctx: TypedActorContext[Natural], msg: Natural): Behavior[Natural] = {
      log.debug(s"msg.value = ${msg.value}")
      Behaviors.same[Natural]
    }

    override def receiveSignal(ctx: TypedActorContext[Natural], msg: Signal): Behavior[Natural] = {
      Behaviors.same[Natural]
    }
  }
}
