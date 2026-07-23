
package domains.sentient

import draco._
import domains._

trait Lean extends Sentient with Primal[(Double, Double)]

object Lean extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Lean", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Lean] = Type[Lean] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
