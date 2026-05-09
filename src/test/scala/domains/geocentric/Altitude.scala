
package domains.geocentric

import draco._
import domains._

trait Altitude extends Geocentric with Primal[Double] 

object Altitude extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Altitude", _namePackage = Seq("domains", "geocentric")))
  lazy val dracoType: Type[Altitude] = Type[Altitude] (typeDefinition)
}
