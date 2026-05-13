
package domains.geocentric.egocentric

import draco._
import domains.geocentric._
import domains._
import domains.egocentric._

trait GeocentricEgocentric extends DomainTransform[Geocentric, Egocentric]

object GeocentricEgocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("GeocentricEgocentric", _namePackage = Seq ("domains", "geocentric", "egocentric")))
  lazy val dracoType: Type[GeocentricEgocentric] = Type[GeocentricEgocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GeocentricEgocentric] = Domain[GeocentricEgocentric] (typeDefinition)
}
