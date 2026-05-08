
package domains.heliocentric.galactocentric

import draco._
import domains.heliocentric._
import domains._
import domains.galactocentric._

trait HeliocentricGalactocentric extends Transform[Heliocentric, Galactocentric]

object HeliocentricGalactocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("HeliocentricGalactocentric", _namePackage = Seq("domains", "heliocentric", "galactocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[HeliocentricGalactocentric] = Domain[HeliocentricGalactocentric] (typeDefinition)
}
