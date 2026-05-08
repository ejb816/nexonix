
package domains.geocentric

import draco._
import domains._
import domains.cosmocentric._

trait Geocentric extends Cosmocentric 

object Geocentric extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Geocentric", _namePackage = Seq("domains", "geocentric")))

  lazy val elementTypeNames: Seq[String] = Seq ("Position", "Altitude", "Heading", "Fix")

  lazy val domainType: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
