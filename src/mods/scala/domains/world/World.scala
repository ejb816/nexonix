package domains.world

import draco._

trait World extends DracoType

object World extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("World", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[World] = Type[World] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)
}
