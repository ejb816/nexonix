package domains.marine

import draco._
import domains.world.World

trait Marine extends World

object Marine extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Marine", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[Marine] = Type[Marine] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("VoyageIntent", "FixReport")

  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)
}
