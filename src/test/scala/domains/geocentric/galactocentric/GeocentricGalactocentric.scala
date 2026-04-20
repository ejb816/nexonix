
package domains.geocentric.galactocentric

import draco._
import domains.geocentric._
import domains._
import domains.galactocentric._

trait GeocentricGalactocentric extends Extensible with Transform[Geocentric, Galactocentric]

object GeocentricGalactocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GeocentricGalactocentric", _namePackage = Seq("domains", "geocentric", "galactocentric")))
  lazy val typeInstance: Type[GeocentricGalactocentric] = Type[GeocentricGalactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[GeocentricGalactocentric] = Domain[GeocentricGalactocentric] (typeDefinition)
}
