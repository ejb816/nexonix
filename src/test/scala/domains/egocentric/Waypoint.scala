
package domains.egocentric

import draco._
import domains._

trait Waypoint extends Egocentric with Primal[(Gaze, Distance)]

object Waypoint extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Waypoint", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Waypoint] = Type[Waypoint] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
