package draco

import org.apache.pekko.actor.typed.ExtensibleBehavior

trait Actor[T] extends ExtensibleBehavior[T] with ActorType

object Actor extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Actor", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Actor[_]] = Type[Actor[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
