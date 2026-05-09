
package domains.galactocentric

import draco._
import domains._

trait ProperMotion extends Galactocentric with Holon[(Double, Double)] 

object ProperMotion extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("ProperMotion", _namePackage = Seq("domains", "galactocentric")))
  lazy val dracoType: Type[ProperMotion] = Type[ProperMotion] (typeDefinition)
}
