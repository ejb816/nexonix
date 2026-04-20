
package domains.geocentric

import draco._
import domains._
import domains.cosmocentric._

trait Geocentric extends Extensible with Cosmocentric 

object Geocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Geocentric", _namePackage = Seq("domains", "geocentric")))
  lazy val typeInstance: Type[Geocentric] = Type[Geocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Position", "Altitude", "Heading", "Fix")

  lazy val domainInstance: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
