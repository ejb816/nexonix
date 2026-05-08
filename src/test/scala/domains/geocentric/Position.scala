
package domains.geocentric

import draco._
import domains._

trait Position extends Geocentric with Holon[(Double, Double)] 

object Position extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Position", _namePackage = Seq("domains", "geocentric")))
  lazy val dracoType: Type[Position] = Type[Position] (typeDefinition)
}
