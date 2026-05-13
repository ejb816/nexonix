
package domains.egocentric

import draco._
import domains._

trait Ego extends Egocentric with Holon[(Percept, Effect)]

object Ego extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Ego", _namePackage = Seq ("domains", "egocentric")))
  lazy val dracoType: Type[Ego] = Type[Ego] (typeDefinition)
  lazy val domainType: Domain[Egocentric] = Domain[Egocentric] (typeDefinition)
}
