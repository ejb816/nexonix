
package domains.egocentric

import draco._
import domains._

trait Course extends Egocentric with Direction

object Course extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Course", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Course] = Type[Course] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
