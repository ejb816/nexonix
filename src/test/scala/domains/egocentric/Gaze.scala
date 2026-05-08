
package domains.egocentric

import draco._
import domains._

trait Gaze extends Egocentric with Direction

object Gaze extends App {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Gaze", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Gaze] = Type[Gaze] (typeDefinition)
}
