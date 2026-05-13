
package domains.galactocentric.geocentric

import draco._
import domains.galactocentric._
import domains._
import domains.geocentric._

trait GalactocentricGeocentric extends DomainTransform[Galactocentric, Geocentric]

object GalactocentricGeocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("GalactocentricGeocentric", _namePackage = Seq ("domains", "galactocentric", "geocentric")))
  lazy val dracoType: Type[GalactocentricGeocentric] = Type[GalactocentricGeocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GalactocentricGeocentric] = Domain[GalactocentricGeocentric] (typeDefinition)
}
