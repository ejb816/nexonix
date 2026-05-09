
package domains.geocentric

import draco._
import domains._

trait Fix extends Geocentric with Holon[(Position, Altitude, Heading)] 

object Fix extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Fix", _namePackage = Seq("domains", "geocentric")))
  lazy val dracoType: Type[Fix] = Type[Fix] (typeDefinition)
}
