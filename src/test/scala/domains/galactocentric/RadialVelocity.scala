
package domains.galactocentric

import draco._
import domains._

trait RadialVelocity extends Extensible with Galactocentric with Primal[Double] 

object RadialVelocity extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("RadialVelocity", _namePackage = Seq("domains", "galactocentric")))
  lazy val typeInstance: Type[RadialVelocity] = Type[RadialVelocity] (typeDefinition)
}
