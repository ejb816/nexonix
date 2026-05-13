
package domains.egocentric

import draco._
import domains._

trait Direction extends Egocentric with Primal[(Double, Double)]

object Direction extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Direction", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Direction] = Type[Direction] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
