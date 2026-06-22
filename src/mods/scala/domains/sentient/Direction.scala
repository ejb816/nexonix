
package domains.sentient

import draco._
import domains._

trait Direction extends Sentient with Primal[(Double, Double)]

object Direction extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Direction", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Direction] = Type[Direction] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
