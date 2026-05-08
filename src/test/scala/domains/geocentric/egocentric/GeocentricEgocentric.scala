
package domains.geocentric.egocentric

import draco._
import domains.geocentric._
import domains._
import domains.egocentric._

trait GeocentricEgocentric extends Transform[Geocentric, Egocentric]

object GeocentricEgocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GeocentricEgocentric", _namePackage = Seq("domains", "geocentric", "egocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GeocentricEgocentric] = Domain[GeocentricEgocentric] (typeDefinition)
}
