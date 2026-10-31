package domains.marine

import draco._
import domains._
import domains.world._

trait Marine extends World

object Marine extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Marine", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[Marine] = Type[Marine] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("VoyageIntent", "FixReport", "ConsumeReport", "OriginateReport", "Consumer", "Creator")

  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)
}
