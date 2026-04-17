
package domains.egocentric

import draco._
import domains._

trait Bearing extends Extensible with Egocentric with Primal[Double] 

object Bearing extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Bearing", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Bearing] = Type[Bearing] (typeDefinition)
}
