
package domains.geocentric.galactocentric

import draco._
import domains.geocentric._
import domains._
import domains.galactocentric._

trait GeocentricGalactocentric extends DomainTransform[Geocentric, Galactocentric]

object GeocentricGalactocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("GeocentricGalactocentric", _namePackage = Seq ("domains", "geocentric", "galactocentric")))
  lazy val dracoType: Type[GeocentricGalactocentric] = Type[GeocentricGalactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GeocentricGalactocentric] = Domain[GeocentricGalactocentric] (typeDefinition)
}
