
package domains.geocentric

import draco._
import domains._

trait Heading extends Geocentric with Primal[Double] 

object Heading extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Heading", _namePackage = Seq ("domains", "geocentric")))
  lazy val dracoType: Type[Heading] = Type[Heading] (typeDefinition)
  lazy val domainType: Domain[Geocentric] = Domain[Geocentric] (typeDefinition)
}
