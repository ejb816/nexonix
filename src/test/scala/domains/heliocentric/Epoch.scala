
package domains.heliocentric

import draco._
import domains._

trait Epoch extends Heliocentric with Primal[Double] 

object Epoch extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Epoch", _namePackage = Seq ("domains", "heliocentric")))
  lazy val dracoType: Type[Epoch] = Type[Epoch] (typeDefinition)
  lazy val domainType: Domain[Heliocentric] = Domain[Heliocentric] (typeDefinition)
}
