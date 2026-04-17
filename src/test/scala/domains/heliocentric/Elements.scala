
package domains.heliocentric

import draco._
import domains._

trait Elements extends Extensible with Heliocentric with Holon[(Double, Double, Double, Double, Double, Double)] 

object Elements extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Elements", _namePackage = Seq("domains", "heliocentric")))
  lazy val typeInstance: Type[Elements] = Type[Elements] (typeDefinition)
}
