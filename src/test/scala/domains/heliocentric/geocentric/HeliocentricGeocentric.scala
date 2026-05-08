
package domains.heliocentric.geocentric

import draco._
import domains.heliocentric._
import domains._
import domains.geocentric._

trait HeliocentricGeocentric extends Transform[Heliocentric, Geocentric]

object HeliocentricGeocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("HeliocentricGeocentric", _namePackage = Seq("domains", "heliocentric", "geocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[HeliocentricGeocentric] = Domain[HeliocentricGeocentric] (typeDefinition)
}
