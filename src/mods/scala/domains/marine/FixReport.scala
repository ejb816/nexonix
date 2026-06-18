package domains.marine

import draco._
import domains._
import draco.format.json._

trait FixReport extends Marine with Json

object FixReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("FixReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[FixReport] = Type[FixReport] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)
}
