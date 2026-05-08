
package domains.egocentric

import draco._
import domains._

trait Ego extends Egocentric with Holon[(Percept, Effect)]

object Ego extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Ego", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Ego] = Type[Ego] (typeDefinition)
}
