package domains.aerial

import draco._
import domains.world.World

trait Aerial extends World

object Aerial extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Aerial", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Aerial] = Type[Aerial] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("FlightIntent", "PositionReport", "ConsumeReport", "OriginateReport")

  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)
}
