
package domains.sentient

import draco._
import domains._

trait Path extends Sentient with Primal[Seq[Waypoint]]

object Path extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Path", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Path] = Type[Path] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
