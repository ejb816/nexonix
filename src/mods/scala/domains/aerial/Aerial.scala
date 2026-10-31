package domains.aerial

import draco._
import domains._
import domains.world._

trait Aerial extends World

object Aerial extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Aerial", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Aerial] = Type[Aerial] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("FlightIntent", "PositionReport", "ConsumeReport", "OriginateReport", "Consumer", "Creator", "Input", "Position")

  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)
}
