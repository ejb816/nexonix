
package domains.galactocentric.heliocentric

import draco._
import domains.galactocentric._
import domains._
import domains.heliocentric._

trait GalactocentricHeliocentric extends DomainTransform[Galactocentric, Heliocentric]

object GalactocentricHeliocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("GalactocentricHeliocentric", _namePackage = Seq ("domains", "galactocentric", "heliocentric")))
  lazy val dracoType: Type[GalactocentricHeliocentric] = Type[GalactocentricHeliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GalactocentricHeliocentric] = Domain[GalactocentricHeliocentric] (typeDefinition)
}
