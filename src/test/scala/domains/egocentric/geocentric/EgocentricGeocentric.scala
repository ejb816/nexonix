
package domains.egocentric.geocentric

import draco._
import domains.egocentric._
import domains._
import domains.geocentric._

trait EgocentricGeocentric extends Transform[Egocentric, Geocentric]

object EgocentricGeocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("EgocentricGeocentric", _namePackage = Seq("domains", "egocentric", "geocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[EgocentricGeocentric] = Domain[EgocentricGeocentric] (typeDefinition)
}
