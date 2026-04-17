
package domains.egocentric

import draco._
import domains._

trait Reach extends Extensible with Egocentric with Primal[Double] 

object Reach extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Reach", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Reach] = Type[Reach] (typeDefinition)
}
