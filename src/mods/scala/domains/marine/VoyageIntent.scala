package domains.marine

import draco._
import domains._
import draco.format.json._

trait VoyageIntent extends Marine with JSON

object VoyageIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("VoyageIntent", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[VoyageIntent] = Type[VoyageIntent] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)
}
