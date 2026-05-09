
package domains.galactocentric

import draco._
import domains._
import domains.cosmocentric._

trait Galactocentric extends Cosmocentric 

object Galactocentric extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Galactocentric", _namePackage = Seq("domains", "galactocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ("Parallax", "ProperMotion", "RadialVelocity", "Trajectory")

  lazy val domainType: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
