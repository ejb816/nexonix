
package domains.geocentric

import draco._
import domains._
import domains.cosmocentric._

trait Geocentric extends Cosmocentric 

object Geocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Geocentric", _namePackage = Seq ("domains", "geocentric")))
  lazy val dracoType: Type[Geocentric] = Type[Geocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Position", "Altitude", "Heading", "Fix")

  lazy val domainType: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
