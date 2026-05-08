
package domains.geocentric.galactocentric

import draco._
import domains.geocentric._
import domains._
import domains.galactocentric._

trait GeocentricGalactocentric extends Transform[Geocentric, Galactocentric]

object GeocentricGalactocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GeocentricGalactocentric", _namePackage = Seq("domains", "geocentric", "galactocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GeocentricGalactocentric] = Domain[GeocentricGalactocentric] (typeDefinition)
}
