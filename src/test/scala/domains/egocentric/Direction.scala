
package domains.egocentric

import draco._
import domains._

trait Direction extends Extensible with Egocentric with Primal[(Double, Double)]

object Direction extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Direction", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Direction] = Type[Direction] (typeDefinition)
}
