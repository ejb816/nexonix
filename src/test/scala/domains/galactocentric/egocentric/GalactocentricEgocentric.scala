
package domains.galactocentric.egocentric

import draco._
import domains.galactocentric._
import domains._
import domains.egocentric._

trait GalactocentricEgocentric extends Transform[Galactocentric, Egocentric]

object GalactocentricEgocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GalactocentricEgocentric", _namePackage = Seq("domains", "galactocentric", "egocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GalactocentricEgocentric] = Domain[GalactocentricEgocentric] (typeDefinition)
}
