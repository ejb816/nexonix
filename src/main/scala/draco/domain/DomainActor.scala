package draco.domain

import org.apache.pekko.actor.typed._
import org.apache.pekko.actor.typed.scaladsl.Behaviors


trait DomainActor[T] extends ExtensibleBehavior[T] {
  override def receive(ctx: TypedActorContext[T], msg: T): Behavior[T] = {
    Behaviors.same
  }
  override def receiveSignal(ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
    Behaviors.same
  }
}

object DomainActor extends App {
  def apply[T](): DomainActor[T] = new DomainActor[T] {
    override def receive(ctx: TypedActorContext[T], msg: T): Behavior[T] = {
      println(s"msg = $msg")
      Behaviors.same
    }

    override def receiveSignal(ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
      Behaviors.same
    }
  }
  val systemBehavior = DomainActor[Int]()
  val system = ActorSystem[Int](systemBehavior, "actorInt")
  val actorInt = system.systemActorOf(systemBehavior,"actorInt")
  actorInt ! 10
}
