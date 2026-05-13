
package domains.egocentric

import draco._
import domains._

trait Gaze extends Egocentric with Direction

object Gaze extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Gaze", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Gaze] = Type[Gaze] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
