
package domains.egocentric

import draco._
import domains._

trait Effect extends Extensible with Egocentric with Primal[Unit]

object Effect extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Effect", _namePackage = Seq("domains", "egocentric")))
  lazy val typeInstance: Type[Effect] = Type[Effect] (typeDefinition)
}
