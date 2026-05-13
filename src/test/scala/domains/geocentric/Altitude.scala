
package domains.geocentric

import draco._
import domains._

trait Altitude extends Geocentric with Primal[Double] 

object Altitude extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Altitude", _namePackage = Seq ("domains", "geocentric")))
  lazy val dracoType: Type[Altitude] = Type[Altitude] (typeDefinition)
  lazy val domainType: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
