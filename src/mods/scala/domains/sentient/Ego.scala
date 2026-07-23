
package domains.sentient

import draco._
import domains._

trait Ego extends Sentient with Holon[(Percept, Effect)]

object Ego extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Ego", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Ego] = Type[Ego] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
