
package domains.egocentric

import draco._
import domains._

trait Waypoint extends Egocentric with Primal[(Gaze, Distance)]

object Waypoint extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Waypoint", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Waypoint] = Type[Waypoint] (typeDefinition)
}
