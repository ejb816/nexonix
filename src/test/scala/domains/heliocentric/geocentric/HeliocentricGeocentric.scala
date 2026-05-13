
package domains.heliocentric.geocentric

import draco._
import domains.heliocentric._
import domains._
import domains.geocentric._

trait HeliocentricGeocentric extends DomainTransform[Heliocentric, Geocentric]

object HeliocentricGeocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("HeliocentricGeocentric", _namePackage = Seq ("domains", "heliocentric", "geocentric")))
  lazy val dracoType: Type[HeliocentricGeocentric] = Type[HeliocentricGeocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[HeliocentricGeocentric] = Domain[HeliocentricGeocentric] (typeDefinition)
}
