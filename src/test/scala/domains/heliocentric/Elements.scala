
package domains.heliocentric

import draco._
import domains._

trait Elements extends Heliocentric with Holon[(Double, Double, Double, Double, Double, Double)] 

object Elements extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Elements", _namePackage = Seq ("domains", "heliocentric")))
  lazy val dracoType: Type[Elements] = Type[Elements] (typeDefinition)
  lazy val domainType: Domain[Heliocentric] = Domain[Heliocentric] (typeDefinition)
}
