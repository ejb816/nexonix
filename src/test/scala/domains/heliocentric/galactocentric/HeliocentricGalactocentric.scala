
package domains.heliocentric.galactocentric

import draco._
import domains.heliocentric._
import domains._
import domains.galactocentric._

trait HeliocentricGalactocentric extends DomainTransform[Heliocentric, Galactocentric]

object HeliocentricGalactocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("HeliocentricGalactocentric", _namePackage = Seq ("domains", "heliocentric", "galactocentric")))
  lazy val dracoType: Type[HeliocentricGalactocentric] = Type[HeliocentricGalactocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[HeliocentricGalactocentric] = Domain[HeliocentricGalactocentric] (typeDefinition)
}
