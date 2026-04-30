
package domains.egocentric

import draco._
import domains._

trait Lean extends Extensible with Egocentric with Primal[(Double, Double)]

object Lean extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Lean", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Lean] = Type[Lean] (typeDefinition)
}
