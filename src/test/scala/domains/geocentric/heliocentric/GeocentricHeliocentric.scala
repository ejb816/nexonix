
package domains.geocentric.heliocentric

import draco._
import domains.geocentric._
import domains._
import domains.heliocentric._

trait GeocentricHeliocentric extends Transform[Geocentric, Heliocentric]

object GeocentricHeliocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GeocentricHeliocentric", _namePackage = Seq("domains", "geocentric", "heliocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GeocentricHeliocentric] = Domain[GeocentricHeliocentric] (typeDefinition)
}
