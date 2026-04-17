
package domains.geocentric

import draco._
import domains._

trait Altitude extends Extensible with Geocentric with Primal[Double] 

object Altitude extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Altitude", _namePackage = Seq("domains", "geocentric")))
  lazy val typeInstance: Type[Altitude] = Type[Altitude] (typeDefinition)
}
