package draco.base

import draco._

trait Rotation[T] extends Cardinal[T]

object Rotation extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Rotation", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Rotation[_]] = Type[Rotation[_]] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
