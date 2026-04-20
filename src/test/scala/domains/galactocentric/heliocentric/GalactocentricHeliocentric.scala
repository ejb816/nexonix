
package domains.galactocentric.heliocentric

import draco._
import domains.galactocentric._
import domains._
import domains.heliocentric._

trait GalactocentricHeliocentric extends Extensible with Transform[Galactocentric, Heliocentric]

object GalactocentricHeliocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GalactocentricHeliocentric", _namePackage = Seq("domains", "galactocentric", "heliocentric")))
  lazy val typeInstance: Type[GalactocentricHeliocentric] = Type[GalactocentricHeliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[GalactocentricHeliocentric] = Domain[GalactocentricHeliocentric] (typeDefinition)
}
