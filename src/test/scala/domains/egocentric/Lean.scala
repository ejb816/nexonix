
package domains.egocentric

import draco._
import domains._

trait Lean extends Egocentric with Primal[(Double, Double)]

object Lean extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Lean", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Lean] = Type[Lean] (typeDefinition)
}
