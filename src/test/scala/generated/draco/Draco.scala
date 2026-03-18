
package generated.draco

import draco._

trait Draco extends DomainInstance 

object Draco extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Draco", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Draco] = Type[Draco] (typeDefinition)
}
