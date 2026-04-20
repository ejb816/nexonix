
package domains.galactocentric.egocentric

import draco._
import domains.galactocentric._
import domains._
import domains.egocentric._

trait GalactocentricEgocentric extends Extensible with Transform[Galactocentric, Egocentric]

object GalactocentricEgocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("GalactocentricEgocentric", _namePackage = Seq("domains", "galactocentric", "egocentric")))
  lazy val typeInstance: Type[GalactocentricEgocentric] = Type[GalactocentricEgocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[GalactocentricEgocentric] = Domain[GalactocentricEgocentric] (typeDefinition)
}
