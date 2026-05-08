
package domains.heliocentric

import draco._
import domains._
import domains.cosmocentric._

trait Heliocentric extends Cosmocentric 

object Heliocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Heliocentric", _namePackage = Seq("domains", "heliocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ("Elements", "Epoch", "Ephemeris")

  lazy val domainType: Domain[Heliocentric] = Domain[Heliocentric] (typeDefinition)
}
