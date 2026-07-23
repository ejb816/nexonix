package draco.scalatarget

import draco._

trait ScalaTarget extends Source

object ScalaTarget extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ScalaTarget", _namePackage = Seq ("draco", "scalatarget")))
  lazy val dracoType: Type[ScalaTarget] = Type[ScalaTarget] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[ScalaTarget] = Domain[ScalaTarget] (typeDefinition)
  lazy val generator: TypeDefinition => String = Generator.generate
}
