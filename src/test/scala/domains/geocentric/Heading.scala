
package domains.geocentric

import draco._
import domains._

trait Heading extends Extensible with Geocentric with Primal[Double] 

object Heading extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Heading", _namePackage = Seq("domains", "geocentric")))
  lazy val typeInstance: Type[Heading] = Type[Heading] (typeDefinition)
}
