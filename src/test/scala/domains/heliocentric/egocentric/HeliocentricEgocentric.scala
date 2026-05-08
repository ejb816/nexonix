
package domains.heliocentric.egocentric

import draco._
import domains.heliocentric._
import domains._
import domains.egocentric._

trait HeliocentricEgocentric extends Transform[Heliocentric, Egocentric]

object HeliocentricEgocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("HeliocentricEgocentric", _namePackage = Seq("domains", "heliocentric", "egocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[HeliocentricEgocentric] = Domain[HeliocentricEgocentric] (typeDefinition)
}
