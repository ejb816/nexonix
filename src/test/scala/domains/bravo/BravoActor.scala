package domains.bravo

import domains.dataModel._
import draco._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait BravoActor extends ActorInstance

object BravoActor extends App with ActorInstance {
  lazy val typeDefinition: TypeDefinition = Bravo.typeDefinition
  lazy val typeInstance: DracoType = Type[Bravo](Bravo.typeDefinition)

  def actorWithProbe(probe: ActorRef[Bravo]): Actor[Bravo] = {
    new Actor[Bravo] {
      override val actorDefinition: TypeDefinition = TypeDefinition(Bravo.typeDefinition.typeName)
      override val typeDefinition: TypeDefinition = Bravo.typeDefinition

      override def receive(ctx: TypedActorContext[Bravo], msg: Bravo): Behavior[Bravo] = {
        msg match {
          case a: Assembled => println(s"BravoActor received: number=${a.number}, text=${a.text}")
          case _ => println(s"BravoActor received: $msg")
        }
        probe ! msg
        Behaviors.same[Bravo]
      }

      override def receiveSignal(ctx: TypedActorContext[Bravo], msg: Signal): Behavior[Bravo] = {
        Behaviors.same[Bravo]
      }
    }
  }

  lazy val actorInstance: ActorType = new Actor[Bravo] {
    override val actorDefinition: TypeDefinition = TypeDefinition(Bravo.typeDefinition.typeName)
    override val typeDefinition: TypeDefinition = Bravo.typeDefinition

    override def receive(ctx: TypedActorContext[Bravo], msg: Bravo): Behavior[Bravo] = {
      println(s"BravoActor received: $msg")
      Behaviors.same[Bravo]
    }

    override def receiveSignal(ctx: TypedActorContext[Bravo], msg: Signal): Behavior[Bravo] = {
      Behaviors.same[Bravo]
    }
  }
}
