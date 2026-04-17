
package domains.galactocentric

import draco._
import domains._

trait Trajectory extends Extensible with Galactocentric with Holon[(Parallax, ProperMotion, RadialVelocity)] 

object Trajectory extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Trajectory", _namePackage = Seq("domains", "galactocentric")))
  lazy val typeInstance: Type[Trajectory] = Type[Trajectory] (typeDefinition)
}
