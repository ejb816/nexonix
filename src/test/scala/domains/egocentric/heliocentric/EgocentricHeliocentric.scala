
package domains.egocentric.heliocentric

import draco._
import domains.egocentric._
import domains._
import domains.heliocentric._

trait EgocentricHeliocentric extends Transform[Egocentric, Heliocentric]

object EgocentricHeliocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("EgocentricHeliocentric", _namePackage = Seq("domains", "egocentric", "heliocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[EgocentricHeliocentric] = Domain[EgocentricHeliocentric] (typeDefinition)
}
