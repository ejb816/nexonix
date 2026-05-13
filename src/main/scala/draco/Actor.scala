package draco

import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.ExtensibleBehavior

trait Actor[T] extends ExtensibleBehavior[T] with ActorType

object Actor extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Actor", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Actor[_]] = Type[Actor[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply[T] (
    _actorDefinition: TypeDefinition
  ) : Actor[T] = new Actor[T] {
    override lazy val typeDefinition: TypeDefinition = _actorDefinition
    override lazy val actorDefinition: TypeDefinition = _actorDefinition
    override def receive(ctx: TypedActorContext[T], msg: T): Behavior[T] = Behaviors.same[T]
    override def receiveSignal(ctx: TypedActorContext[T], msg: Signal): Behavior[T] = Behaviors.same[T]
  }

  lazy val Null: Actor[_] = apply[Nothing](
    _actorDefinition = null.asInstanceOf[TypeDefinition]
  )

}
