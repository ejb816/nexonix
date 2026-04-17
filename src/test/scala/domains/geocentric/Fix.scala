
package domains.geocentric

import draco._
import domains._

trait Fix extends Extensible with Geocentric with Holon[(Position, Altitude, Heading)] 

object Fix extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Fix", _namePackage = Seq("domains", "geocentric")))
  lazy val typeInstance: Type[Fix] = Type[Fix] (typeDefinition)
}
