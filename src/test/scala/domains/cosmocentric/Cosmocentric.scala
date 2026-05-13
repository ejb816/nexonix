
package domains.cosmocentric

import draco._
import domains._

trait Cosmocentric extends DomainType

object Cosmocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Cosmocentric", _namePackage = Seq ("domains", "cosmocentric")))
  lazy val dracoType: Type[Cosmocentric] = Type[Cosmocentric] (typeDefinition)
  lazy val domainType: Domain[Cosmocentric] = Domain[Cosmocentric] (typeDefinition)
}
