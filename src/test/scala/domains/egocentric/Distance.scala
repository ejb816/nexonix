
package domains.egocentric

import draco._
import domains._

trait Distance extends Extensible with Egocentric with Primal[Double] 

object Distance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Distance", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Distance] = Type[Distance] (typeDefinition)
}
