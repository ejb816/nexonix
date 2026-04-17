
package domains.galactocentric

import draco._
import domains._

trait ProperMotion extends Extensible with Galactocentric with Holon[(Double, Double)] 

object ProperMotion extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("ProperMotion", _namePackage = Seq("domains", "galactocentric")))
  lazy val typeInstance: Type[ProperMotion] = Type[ProperMotion] (typeDefinition)
}
