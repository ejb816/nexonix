
package domains.egocentric

import draco._
import domains._

trait Percept extends Egocentric with Primal[(Gaze, Distance)]

object Percept extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Percept", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Percept] = Type[Percept] (typeDefinition)
}
