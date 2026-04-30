
package domains.egocentric

import draco._
import domains._

trait Ego extends Extensible with Egocentric with Holon[(Percept, Effect)]

object Ego extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Ego", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Ego] = Type[Ego] (typeDefinition)
}
