
package domains.geocentric

import draco._
import domains._

trait Heading extends Geocentric with Primal[Double] 

object Heading extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Heading", _namePackage = Seq("domains", "geocentric")))
  lazy val dracoType: Type[Heading] = Type[Heading] (typeDefinition)
}
