
package domains.cosmocentric

import draco._
import domains._

trait Cosmocentric extends Extensible with DomainInstance 

object Cosmocentric extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Cosmocentric", _namePackage = Seq("domains", "cosmocentric")))
  lazy val typeInstance: Type[Cosmocentric] = Type[Cosmocentric] (typeDefinition)
}
