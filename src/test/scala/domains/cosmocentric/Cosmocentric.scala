
package domains.cosmocentric

import draco._
import domains._

trait Cosmocentric extends DracoType

object Cosmocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Cosmocentric", _namePackage = Seq("domains", "cosmocentric")))
  lazy val dracoType: Type[Cosmocentric] = Type[Cosmocentric] (typeDefinition)
}
