
package domains.egocentric

import draco._
import domains._

trait Course extends Egocentric with Direction

object Course extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Course", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Course] = Type[Course] (typeDefinition)
}
