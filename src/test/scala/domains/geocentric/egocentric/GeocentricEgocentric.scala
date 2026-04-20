
package domains.geocentric.egocentric

import draco._
import domains.geocentric._
import domains._
import domains.egocentric._

trait GeocentricEgocentric extends Extensible with Transform[Geocentric, Egocentric]

object GeocentricEgocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GeocentricEgocentric", _namePackage = Seq("domains", "geocentric", "egocentric")))
  lazy val typeInstance: Type[GeocentricEgocentric] = Type[GeocentricEgocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[GeocentricEgocentric] = Domain[GeocentricEgocentric] (typeDefinition)
}
