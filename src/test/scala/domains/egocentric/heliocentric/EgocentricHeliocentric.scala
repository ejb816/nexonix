
package domains.egocentric.heliocentric

import draco._
import domains.egocentric._
import domains._
import domains.heliocentric._

trait EgocentricHeliocentric extends Extensible with Transform[Egocentric, Heliocentric]

object EgocentricHeliocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("EgocentricHeliocentric", _namePackage = Seq("domains", "egocentric", "heliocentric")))
  lazy val typeInstance: Type[EgocentricHeliocentric] = Type[EgocentricHeliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[EgocentricHeliocentric] = Domain[EgocentricHeliocentric] (typeDefinition)
}
