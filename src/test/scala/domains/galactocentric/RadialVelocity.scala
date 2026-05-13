
package domains.galactocentric

import draco._
import domains._

trait RadialVelocity extends Galactocentric with Primal[Double] 

object RadialVelocity extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("RadialVelocity", _namePackage = Seq ("domains", "galactocentric")))
  lazy val dracoType: Type[RadialVelocity] = Type[RadialVelocity] (typeDefinition)
  lazy val domainType: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
