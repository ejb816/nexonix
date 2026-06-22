
package domains.sentient

import draco._
import domains._
import domains.world._

trait Sentient extends World 

object Sentient extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Sentient", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Sentient] = Type[Sentient] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Direction", "Distance", "Course", "Gaze", "Percept", "Lean", "Effect", "Waypoint", "Path", "Ego")

  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
