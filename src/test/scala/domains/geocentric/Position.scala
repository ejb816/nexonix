
package domains.geocentric

import draco._
import domains._

trait Position extends Extensible with Geocentric with Holon[(Double, Double)] 

object Position extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Position", _namePackage = Seq("domains", "geocentric")))
  lazy val typeInstance: Type[Position] = Type[Position] (typeDefinition)
}
