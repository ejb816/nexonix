
package domains.galactocentric

import draco._
import domains._

trait Trajectory extends Galactocentric with Holon[(Parallax, ProperMotion, RadialVelocity)] 

object Trajectory extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Trajectory", _namePackage = Seq("domains", "galactocentric")))
  lazy val dracoType: Type[Trajectory] = Type[Trajectory] (typeDefinition)
}
