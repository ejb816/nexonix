
package domains.heliocentric

import draco._
import domains._

trait Elements extends Heliocentric with Holon[(Double, Double, Double, Double, Double, Double)] 

object Elements extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Elements", _namePackage = Seq("domains", "heliocentric")))
  lazy val dracoType: Type[Elements] = Type[Elements] (typeDefinition)
}
