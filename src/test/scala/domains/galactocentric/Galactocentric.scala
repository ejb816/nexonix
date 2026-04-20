
package domains.galactocentric

import draco._
import domains._
import domains.cosmocentric._

trait Galactocentric extends Extensible with Cosmocentric 

object Galactocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Galactocentric", _namePackage = Seq("domains", "galactocentric")))
  lazy val typeInstance: Type[Galactocentric] = Type[Galactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Parallax", "ProperMotion", "RadialVelocity", "Trajectory")

  lazy val domainInstance: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
