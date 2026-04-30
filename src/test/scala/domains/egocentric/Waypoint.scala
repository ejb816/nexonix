
package domains.egocentric

import draco._
import domains._

trait Waypoint extends Extensible with Egocentric with Primal[(Gaze, Distance)]

object Waypoint extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Waypoint", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Waypoint] = Type[Waypoint] (typeDefinition)
}
