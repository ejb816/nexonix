
package domains.heliocentric.geocentric

import draco._
import domains.heliocentric._
import domains._
import domains.geocentric._

trait HeliocentricGeocentric extends Extensible with Transform[Heliocentric, Geocentric]

object HeliocentricGeocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("HeliocentricGeocentric", _namePackage = Seq("domains", "heliocentric", "geocentric")))
  lazy val typeInstance: Type[HeliocentricGeocentric] = Type[HeliocentricGeocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[HeliocentricGeocentric] = Domain[HeliocentricGeocentric] (typeDefinition)
}
