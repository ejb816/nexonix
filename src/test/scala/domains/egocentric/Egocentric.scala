
package domains.egocentric

import draco._
import domains._
import domains.cosmocentric._

trait Egocentric extends Extensible with Cosmocentric 

object Egocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Egocentric", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Egocentric] = Type[Egocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Direction", "Distance", "Course", "Gaze", "Percept", "Lean", "Effect", "Waypoint", "Path", "Ego")

  lazy val domainInstance: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
