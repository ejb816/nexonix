
package domains.egocentric.galactocentric

import draco._
import domains.egocentric._
import domains._
import domains.galactocentric._

trait EgocentricGalactocentric extends Transform[Egocentric, Galactocentric]

object EgocentricGalactocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("EgocentricGalactocentric", _namePackage = Seq("domains", "egocentric", "galactocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[EgocentricGalactocentric] = Domain[EgocentricGalactocentric] (typeDefinition)
}
