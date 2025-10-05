package draco

import org.apache.pekko.actor.typed.{Behavior, ExtensibleBehavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait ActorBehavior[T] extends ExtensibleBehavior[T] {
  override def receive (ctx: TypedActorContext[T], msg: T): Behavior[T] = {
    Behaviors.same
  }
  override def receiveSignal (ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
    Behaviors.same
  }
}

object ActorBehavior {
  def apply[T](): ActorBehavior[T] = new ActorBehavior[T] {
    override def receive (ctx: TypedActorContext[T], msg: T): Behavior[T] = {
      println(s"msg = $msg")
      Behaviors.same
    }

    override def receiveSignal (ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
      Behaviors.same
    }
  }
}
