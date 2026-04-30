
package domains.egocentric

import draco._
import domains._

trait Gaze extends Extensible with Egocentric with Direction

object Gaze extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Gaze", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Gaze] = Type[Gaze] (typeDefinition)
}
