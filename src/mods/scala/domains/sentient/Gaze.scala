
package domains.sentient

import draco._
import domains._

trait Gaze extends Sentient with Direction

object Gaze extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Gaze", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Gaze] = Type[Gaze] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
