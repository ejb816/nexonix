
package domains.galactocentric

import draco._
import domains._

trait Trajectory extends Galactocentric with Holon[(Parallax, ProperMotion, RadialVelocity)] 

object Trajectory extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Trajectory", _namePackage = Seq ("domains", "galactocentric")))
  lazy val dracoType: Type[Trajectory] = Type[Trajectory] (typeDefinition)
  lazy val domainType: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
