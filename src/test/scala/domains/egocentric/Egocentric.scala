
package domains.egocentric

import draco._
import domains._
import domains.cosmocentric._

trait Egocentric extends Cosmocentric 

object Egocentric extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Egocentric", _namePackage = Seq("domains", "egocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ("Direction", "Distance", "Course", "Gaze", "Percept", "Lean", "Effect", "Waypoint", "Path", "Ego")

  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
