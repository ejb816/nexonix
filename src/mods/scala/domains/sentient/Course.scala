
package domains.sentient

import draco._
import domains._

trait Course extends Sentient with Direction

object Course extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Course", _namePackage = Seq ("domains", "sentient")))
  lazy val dracoType: Type[Course] = Type[Course] (typeDefinition)
  lazy val domainType: Domain[Sentient] = Domain[Sentient] (typeDefinition)
}
