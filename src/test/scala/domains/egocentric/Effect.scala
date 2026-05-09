
package domains.egocentric

import draco._
import domains._

trait Effect extends Egocentric with Primal[Unit]

object Effect extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Effect", _namePackage = Seq("domains", "egocentric")))
  lazy val dracoType: Type[Effect] = Type[Effect] (typeDefinition)
}
