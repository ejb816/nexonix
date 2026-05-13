
package domains.egocentric

import draco._
import domains._

trait Lean extends Egocentric with Primal[(Double, Double)]

object Lean extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Lean", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Lean] = Type[Lean] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
