
package domains.heliocentric

import draco._
import domains._

trait Epoch extends Extensible with Heliocentric with Primal[Double] 

object Epoch extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Epoch", _namePackage = Seq("domains", "heliocentric")))
  lazy val typeInstance: Type[Epoch] = Type[Epoch] (typeDefinition)
}
