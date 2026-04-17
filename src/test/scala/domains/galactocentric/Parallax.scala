
package domains.galactocentric

import draco._
import domains._

trait Parallax extends Extensible with Galactocentric with Primal[Double] 

object Parallax extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Parallax", _namePackage = Seq("domains", "galactocentric")))
  lazy val typeInstance: Type[Parallax] = Type[Parallax] (typeDefinition)
}
