
package domains.geocentric

import draco._
import domains._

trait Position extends Geocentric with Holon[(Double, Double)] 

object Position extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Position", _namePackage = Seq ("domains", "geocentric")))
  lazy val dracoType: Type[Position] = Type[Position] (typeDefinition)
  lazy val domainType: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
