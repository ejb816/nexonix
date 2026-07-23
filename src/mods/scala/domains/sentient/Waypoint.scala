
package domains.sentient

import draco._
import domains._

trait Waypoint extends Sentient with Primal[(Gaze, Distance)]

object Waypoint extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Waypoint", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Waypoint] = Type[Waypoint] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
