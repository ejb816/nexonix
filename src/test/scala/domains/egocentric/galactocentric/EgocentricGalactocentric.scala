
package domains.egocentric.galactocentric

import draco._
import domains.egocentric._
import domains._
import domains.galactocentric._

trait EgocentricGalactocentric extends Extensible with Transform[Egocentric, Galactocentric]

object EgocentricGalactocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("EgocentricGalactocentric", _namePackage = Seq("domains", "egocentric", "galactocentric")))
  lazy val typeInstance: Type[EgocentricGalactocentric] = Type[EgocentricGalactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[EgocentricGalactocentric] = Domain[EgocentricGalactocentric] (typeDefinition)
}
