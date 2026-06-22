
package domains.sentient

import draco._
import domains._

trait Distance extends Sentient with Primal[Double] 

object Distance extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Distance", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Distance] = Type[Distance] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
