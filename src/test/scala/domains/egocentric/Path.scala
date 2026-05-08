
package domains.egocentric

import draco._
import domains._

trait Path extends Egocentric with Primal[Seq[Waypoint]]

object Path extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Path", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Path] = Type[Path] (typeDefinition)
}
