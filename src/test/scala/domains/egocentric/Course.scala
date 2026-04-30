
package domains.egocentric

import draco._
import domains._

trait Course extends Extensible with Egocentric with Direction

object Course extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Course", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Course] = Type[Course] (typeDefinition)
}
