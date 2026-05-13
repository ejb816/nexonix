
package domains.geocentric

import draco._
import domains._

trait Fix extends Geocentric with Holon[(Position, Altitude, Heading)] 

object Fix extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Fix", _namePackage = Seq ("domains", "geocentric")))
  lazy val dracoType: Type[Fix] = Type[Fix] (typeDefinition)
  lazy val domainType: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
