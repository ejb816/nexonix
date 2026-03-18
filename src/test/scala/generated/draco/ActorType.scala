
package generated.draco

import draco._

trait ActorType extends DracoType {
  val actorDefinition: ActorDefinition
}

object ActorType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("ActorType", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[ActorType] = Type[ActorType] (typeDefinition)
}
