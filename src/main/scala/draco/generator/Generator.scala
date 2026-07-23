package draco.generator

import draco._

trait Generator[L] extends DracoType {
  val generator: TypeDefinition => String
}

object Generator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Generator", _namePackage = Seq ("draco", "generator")))
  lazy val dracoType: Type[Generator[_]] = Type[Generator[_]] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[Generator[_]] = Domain[Generator[_]] (typeDefinition)
}
