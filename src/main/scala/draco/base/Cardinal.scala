package draco.base

import draco._

trait Cardinal[T] extends Unit with Primal[T]

object Cardinal extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Cardinal", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Cardinal[_]] = Type[Cardinal[_]] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
