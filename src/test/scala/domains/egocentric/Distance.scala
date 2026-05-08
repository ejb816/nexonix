
package domains.egocentric

import draco._
import domains._

trait Distance extends Egocentric with Primal[Double] 

object Distance extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Distance", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Distance] = Type[Distance] (typeDefinition)
}
