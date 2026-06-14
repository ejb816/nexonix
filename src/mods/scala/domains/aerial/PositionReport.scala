package domains.aerial

import draco._
import domains._
import draco.format.json._

trait PositionReport extends Aerial with Json

object PositionReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("PositionReport", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[PositionReport] = Type[PositionReport] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)
}
