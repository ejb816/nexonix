package draco.base

import draco._

trait Unit extends DracoType

object Unit extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Unit", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Unit] = Type[Unit] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
