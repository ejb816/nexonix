
package domains.heliocentric

import draco._
import domains._
import domains.cosmocentric._

trait Heliocentric extends Cosmocentric 

object Heliocentric extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Heliocentric", _namePackage = Seq ("domains", "heliocentric")))
  lazy val dracoType: Type[Heliocentric] = Type[Heliocentric] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Elements", "Epoch", "Ephemeris")

  lazy val domainType: Domain[Heliocentric] = Domain[Heliocentric] (typeDefinition)
}
