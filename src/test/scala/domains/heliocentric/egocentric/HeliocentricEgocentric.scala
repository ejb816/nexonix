
package domains.heliocentric.egocentric

import draco._
import domains.heliocentric._
import domains._
import domains.egocentric._

trait HeliocentricEgocentric extends DomainTransform[Heliocentric, Egocentric]

object HeliocentricEgocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("HeliocentricEgocentric", _namePackage = Seq ("domains", "heliocentric", "egocentric")))
  lazy val dracoType: Type[HeliocentricEgocentric] = Type[HeliocentricEgocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[HeliocentricEgocentric] = Domain[HeliocentricEgocentric] (typeDefinition)
}
