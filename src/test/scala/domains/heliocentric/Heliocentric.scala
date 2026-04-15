
package domains.heliocentric

import draco._
import domains._
import domains.cosmocentric._

trait Heliocentric extends Extensible with Cosmocentric 

object Heliocentric extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Heliocentric", _namePackage = Seq("domains", "heliocentric")))
  lazy val typeInstance: Type[Heliocentric] = Type[Heliocentric] (typeDefinition)
}
