
package domains.galactocentric

import draco._
import domains._

trait ProperMotion extends Galactocentric with Holon[(Double, Double)] 

object ProperMotion extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("ProperMotion", _namePackage = Seq ("domains", "galactocentric")))
  lazy val dracoType: Type[ProperMotion] = Type[ProperMotion] (typeDefinition)
  lazy val domainType: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
