
package domains.egocentric

import draco._
import domains._

trait Percept extends Extensible with Egocentric with Holon[(Bearing, Reach)] 

object Percept extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Percept", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Percept] = Type[Percept] (typeDefinition)
}
