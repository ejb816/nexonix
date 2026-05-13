
package domains.geocentric.heliocentric

import draco._
import domains.geocentric._
import domains._
import domains.heliocentric._

trait GeocentricHeliocentric extends DomainTransform[Geocentric, Heliocentric]

object GeocentricHeliocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("GeocentricHeliocentric", _namePackage = Seq ("domains", "geocentric", "heliocentric")))
  lazy val dracoType: Type[GeocentricHeliocentric] = Type[GeocentricHeliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GeocentricHeliocentric] = Domain[GeocentricHeliocentric] (typeDefinition)
}
