package domains.aerial

import draco._
import domains._
import draco.format.dracon._

trait FlightIntent extends Aerial with Dracon

object FlightIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FlightIntent", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[FlightIntent] = Type[FlightIntent] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)
}
