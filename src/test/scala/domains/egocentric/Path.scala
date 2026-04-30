
package domains.egocentric

import draco._
import domains._

trait Path extends Extensible with Egocentric with Primal[Seq[Waypoint]]

object Path extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Path", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Path] = Type[Path] (typeDefinition)
}
