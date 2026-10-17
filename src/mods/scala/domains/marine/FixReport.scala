package domains.marine

import draco._
import domains._
import draco.format.dracon._

trait FixReport extends Marine with Dracon

object FixReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FixReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[FixReport] = Type[FixReport] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)
}
