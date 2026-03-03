package draco

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, ExtensibleBehavior, Signal, TypedActorContext}

trait ActorBehavior[T] extends ExtensibleBehavior[T] {
  override def receive (ctx: TypedActorContext[T], msg: T): Behavior[T] = {
    Behaviors.same
  }
  override def receiveSignal (ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
    Behaviors.same
  }
}

object ActorBehavior extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "ActorBehavior[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("ExtensibleBehavior[T]", _namePackage = Seq ("org", "apache", "pekko", "actor", "typed"))
    ),
    _elements = Seq (
      Dynamic ("receive", "Behavior[T]", Seq (
        Parameter ("ctx", "TypedActorContext[T]", ""),
        Parameter ("msg", "T", "")
      ), Seq.empty),
      Dynamic ("receiveSignal", "Behavior[T]", Seq (
        Parameter ("ctx", "TypedActorContext[T]", ""),
        Parameter ("msg", "Signal", "")
      ), Seq.empty)
    ),
    _factory = Factory ("ActorBehavior[T]")
  )
  lazy val typeInstance: Type[ActorBehavior[_]] = Type[ActorBehavior[_]] (typeDefinition)
  def apply[T](): ActorBehavior[T] = new ActorBehavior[T] {
    // Provisional until type parameters are handled in TypeName
    override def receive (ctx: TypedActorContext[T], msg: T): Behavior[T] = {
      println(s"msg = $msg")
      Behaviors.same
    }

    override def receiveSignal (ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
      Behaviors.same
    }
  }
}
