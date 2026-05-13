
package domains.egocentric.heliocentric

import draco._
import domains.egocentric._
import domains._
import domains.heliocentric._

trait EgocentricHeliocentric extends DomainTransform[Egocentric, Heliocentric]

object EgocentricHeliocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("EgocentricHeliocentric", _namePackage = Seq ("domains", "egocentric", "heliocentric")))
  lazy val dracoType: Type[EgocentricHeliocentric] = Type[EgocentricHeliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[EgocentricHeliocentric] = Domain[EgocentricHeliocentric] (typeDefinition)
}
