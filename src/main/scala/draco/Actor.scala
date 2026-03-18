package draco

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, ExtensibleBehavior, Signal, TypedActorContext}

trait Actor[T] extends ExtensibleBehavior[T] with ActorType

object Actor extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Actor[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("ExtensibleBehavior[T]", _namePackage = Seq ("org", "apache", "pekko", "actor", "typed"))
    ),
    _factory = Factory ("Actor[T]")
  )
  lazy val typeInstance: Type[Actor[_]] = Type[Actor[_]] (typeDefinition)
  def apply[T](
                _actorDefinition: ActorDefinition
              ): Actor[T] = new Actor[T]  {
    val typeDefinition: TypeDefinition = TypeDefinition (_actorDefinition.typeName)
    val actorDefinition: ActorDefinition = _actorDefinition
    def receive(ctx: TypedActorContext[T], msg: T): Behavior[T] = {
      Behaviors.same[T]
    }
    def receiveSignal(ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
      Behaviors.same[T]
    }
  }
}
