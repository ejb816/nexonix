
package domains.egocentric

import draco._
import domains._
import domains.cosmocentric._

trait Egocentric extends Extensible with Cosmocentric 

object Egocentric extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Egocentric", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Egocentric] = Type[Egocentric] (typeDefinition)
}
