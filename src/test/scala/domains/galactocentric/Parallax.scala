
package domains.galactocentric

import draco._
import domains._

trait Parallax extends Galactocentric with Primal[Double] 

object Parallax extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Parallax", _namePackage = Seq("domains", "galactocentric")))
  lazy val dracoType: Type[Parallax] = Type[Parallax] (typeDefinition)
}
