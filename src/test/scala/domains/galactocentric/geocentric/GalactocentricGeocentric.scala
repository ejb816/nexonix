
package domains.galactocentric.geocentric

import draco._
import domains.galactocentric._
import domains._
import domains.geocentric._

trait GalactocentricGeocentric extends Transform[Galactocentric, Geocentric]

object GalactocentricGeocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GalactocentricGeocentric", _namePackage = Seq("domains", "galactocentric", "geocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GalactocentricGeocentric] = Domain[GalactocentricGeocentric] (typeDefinition)
}
