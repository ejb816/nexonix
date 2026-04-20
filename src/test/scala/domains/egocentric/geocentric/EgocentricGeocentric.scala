
package domains.egocentric.geocentric

import draco._
import domains.egocentric._
import domains._
import domains.geocentric._

trait EgocentricGeocentric extends Extensible with Transform[Egocentric, Geocentric]

object EgocentricGeocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("EgocentricGeocentric", _namePackage = Seq("domains", "egocentric", "geocentric")))
  lazy val typeInstance: Type[EgocentricGeocentric] = Type[EgocentricGeocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[EgocentricGeocentric] = Domain[EgocentricGeocentric] (typeDefinition)
}
