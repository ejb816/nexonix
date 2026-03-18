
package generated.draco

import draco._

trait ActorInstance extends TypeInstance {
  val actorInstance: ActorType
}

object ActorInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("ActorInstance", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[ActorInstance] = Type[ActorInstance] (typeDefinition)
}
