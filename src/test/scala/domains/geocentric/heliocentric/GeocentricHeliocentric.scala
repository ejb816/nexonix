
package domains.geocentric.heliocentric

import draco._
import domains.geocentric._
import domains._
import domains.heliocentric._

trait GeocentricHeliocentric extends Extensible with Transform[Geocentric, Heliocentric]

object GeocentricHeliocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GeocentricHeliocentric", _namePackage = Seq("domains", "geocentric", "heliocentric")))
  lazy val typeInstance: Type[GeocentricHeliocentric] = Type[GeocentricHeliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[GeocentricHeliocentric] = Domain[GeocentricHeliocentric] (typeDefinition)
}
