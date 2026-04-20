
package domains.galactocentric.geocentric

import draco._
import domains.galactocentric._
import domains._
import domains.geocentric._

trait GalactocentricGeocentric extends Extensible with Transform[Galactocentric, Geocentric]

object GalactocentricGeocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GalactocentricGeocentric", _namePackage = Seq("domains", "galactocentric", "geocentric")))
  lazy val typeInstance: Type[GalactocentricGeocentric] = Type[GalactocentricGeocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[GalactocentricGeocentric] = Domain[GalactocentricGeocentric] (typeDefinition)
}
