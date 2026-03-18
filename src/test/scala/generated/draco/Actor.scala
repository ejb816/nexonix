
package generated.draco

import draco._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait Actor[T] extends ExtensibleBehavior[T] with ActorType 

object Actor extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Actor", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val typeInstance: Type[Actor[_]] = Type[Actor[_]] (typeDefinition)

  def apply[T] () : Actor[T] = new Actor[T] {
    override lazy val typeInstance: DracoType = Actor.typeInstance
    override lazy val typeDefinition: TypeDefinition = Actor.typeDefinition
  }

  lazy val Null: Actor[_] = apply[Nothing]()


}
