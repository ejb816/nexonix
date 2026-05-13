
package domains.galactocentric.egocentric

import draco._
import domains.galactocentric._
import domains._
import domains.egocentric._

trait GalactocentricEgocentric extends DomainTransform[Galactocentric, Egocentric]

object GalactocentricEgocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("GalactocentricEgocentric", _namePackage = Seq ("domains", "galactocentric", "egocentric")))
  lazy val dracoType: Type[GalactocentricEgocentric] = Type[GalactocentricEgocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GalactocentricEgocentric] = Domain[GalactocentricEgocentric] (typeDefinition)
}
