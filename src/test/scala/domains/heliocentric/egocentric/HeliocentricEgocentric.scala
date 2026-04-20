
package domains.heliocentric.egocentric

import draco._
import domains.heliocentric._
import domains._
import domains.egocentric._

trait HeliocentricEgocentric extends Extensible with Transform[Heliocentric, Egocentric]

object HeliocentricEgocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("HeliocentricEgocentric", _namePackage = Seq("domains", "heliocentric", "egocentric")))
  lazy val typeInstance: Type[HeliocentricEgocentric] = Type[HeliocentricEgocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainInstance: Domain[HeliocentricEgocentric] = Domain[HeliocentricEgocentric] (typeDefinition)
}
