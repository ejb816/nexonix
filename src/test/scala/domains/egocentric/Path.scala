
package domains.egocentric

import draco._
import domains._

trait Path extends Egocentric with Primal[Seq[Waypoint]]

object Path extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Path", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Path] = Type[Path] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
