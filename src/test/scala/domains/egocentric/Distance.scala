
package domains.egocentric

import draco._
import domains._

trait Distance extends Egocentric with Primal[Double] 

object Distance extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Distance", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Distance] = Type[Distance] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
