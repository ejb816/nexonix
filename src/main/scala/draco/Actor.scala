package draco

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, ExtensibleBehavior, Signal, TypedActorContext}

trait Actor[T] extends ExtensibleBehavior[T] with ActorType

object Actor extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Actor", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Actor[_]] = Type[Actor[_]] (typeDefinition)

  def apply[T] (
                 _actorDefinition: TypeDefinition
               ) : Actor[T] = new Actor[T] {
    override val typeDefinition: TypeDefinition = _actorDefinition
    override val actorDefinition: TypeDefinition = _actorDefinition
    override def receive(ctx: TypedActorContext[T], msg: T): Behavior[T] = {
      Behaviors.same[T]
    }
    override def receiveSignal(ctx: TypedActorContext[T], msg: Signal): Behavior[T] = {
      Behaviors.same[T]
    }
  }
}
