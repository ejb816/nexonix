
package domains.heliocentric.galactocentric

import draco._
import domains.heliocentric._
import domains._
import domains.galactocentric._

trait HeliocentricGalactocentric extends Extensible with Transform[Heliocentric, Galactocentric]

object HeliocentricGalactocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("HeliocentricGalactocentric", _namePackage = Seq("domains", "heliocentric", "galactocentric")))
  lazy val typeInstance: Type[HeliocentricGalactocentric] = Type[HeliocentricGalactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[HeliocentricGalactocentric] = Domain[HeliocentricGalactocentric] (typeDefinition)
}
