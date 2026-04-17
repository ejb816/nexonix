
package domains.heliocentric

import draco._
import domains._

trait Ephemeris extends Extensible with Heliocentric with Holon[(Elements, Epoch)] 

object Ephemeris extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Ephemeris", _namePackage = Seq("domains", "heliocentric")))
  lazy val typeInstance: Type[Ephemeris] = Type[Ephemeris] (typeDefinition)
}
