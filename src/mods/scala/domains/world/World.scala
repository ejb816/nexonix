package domains.world

import draco._
import domains._

trait World extends DracoType

object World extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("World", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[World] = Type[World] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Cartesian", "Consumer", "Observable", "Provider")

  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)
}
