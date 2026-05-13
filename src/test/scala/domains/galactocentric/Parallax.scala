
package domains.galactocentric

import draco._
import domains._

trait Parallax extends Galactocentric with Primal[Double] 

object Parallax extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Parallax", _namePackage = Seq ("domains", "galactocentric")))
  lazy val dracoType: Type[Parallax] = Type[Parallax] (typeDefinition)
  lazy val domainType: Domain[Galactocentric] = Domain[Galactocentric] (typeDefinition)
}
