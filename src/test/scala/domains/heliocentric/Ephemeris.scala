
package domains.heliocentric

import draco._
import domains._

trait Ephemeris extends Heliocentric with Holon[(Elements, Epoch)] 

object Ephemeris extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Ephemeris", _namePackage = Seq("domains", "heliocentric")))
  lazy val dracoType: Type[Ephemeris] = Type[Ephemeris] (typeDefinition)
}
