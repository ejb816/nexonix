package draco.base

import draco._

trait Ordinal extends Unit with Primal[Enumeration]

object Ordinal extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Ordinal", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Ordinal] = Type[Ordinal] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
