
package domains.sentient

import draco._
import domains._

trait Percept extends Sentient with Primal[(Gaze, Distance)]

object Percept extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Percept", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Percept] = Type[Percept] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
