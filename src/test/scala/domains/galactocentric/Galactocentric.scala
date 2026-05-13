
package domains.galactocentric

import draco._
import domains._
import domains.cosmocentric._

trait Galactocentric extends Cosmocentric 

object Galactocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Galactocentric", _namePackage = Seq ("domains", "galactocentric")))
  lazy val dracoType: Type[Galactocentric] = Type[Galactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Parallax", "ProperMotion", "RadialVelocity", "Trajectory")

  lazy val domainType: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
