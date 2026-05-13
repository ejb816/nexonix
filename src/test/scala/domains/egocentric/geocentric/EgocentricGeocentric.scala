
package domains.egocentric.geocentric

import draco._
import domains.egocentric._
import domains._
import domains.geocentric._

trait EgocentricGeocentric extends DomainTransform[Egocentric, Geocentric]

object EgocentricGeocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("EgocentricGeocentric", _namePackage = Seq ("domains", "egocentric", "geocentric")))
  lazy val dracoType: Type[EgocentricGeocentric] = Type[EgocentricGeocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[EgocentricGeocentric] = Domain[EgocentricGeocentric] (typeDefinition)
}
