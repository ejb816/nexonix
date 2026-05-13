
package domains.egocentric.galactocentric

import draco._
import domains.egocentric._
import domains._
import domains.galactocentric._

trait EgocentricGalactocentric extends DomainTransform[Egocentric, Galactocentric]

object EgocentricGalactocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("EgocentricGalactocentric", _namePackage = Seq ("domains", "egocentric", "galactocentric")))
  lazy val dracoType: Type[EgocentricGalactocentric] = Type[EgocentricGalactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[EgocentricGalactocentric] = Domain[EgocentricGalactocentric] (typeDefinition)
}
