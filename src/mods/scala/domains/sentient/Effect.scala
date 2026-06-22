
package domains.sentient

import draco._
import domains._

trait Effect extends Sentient with Primal[Unit]

object Effect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Effect", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Effect] = Type[Effect] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
