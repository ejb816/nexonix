
package domains.galactocentric.heliocentric

import draco._
import domains.galactocentric._
import domains._
import domains.heliocentric._

trait GalactocentricHeliocentric extends Transform[Galactocentric, Heliocentric]

object GalactocentricHeliocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GalactocentricHeliocentric", _namePackage = Seq("domains", "galactocentric", "heliocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GalactocentricHeliocentric] = Domain[GalactocentricHeliocentric] (typeDefinition)
}
