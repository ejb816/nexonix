
package domains.heliocentric

import draco._
import domains._

trait Epoch extends Heliocentric with Primal[Double] 

object Epoch extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Epoch", _namePackage = Seq("domains", "heliocentric")))
  lazy val dracoType: Type[Epoch] = Type[Epoch] (typeDefinition)
}
