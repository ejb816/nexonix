package draco.base

import draco._

trait Distance[T] extends Cardinal[T]

object Distance extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Distance", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Distance[_]] = Type[Distance[_]] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
