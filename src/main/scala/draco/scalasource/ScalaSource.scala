package draco.scalasource

import draco._

trait ScalaSource extends DracoType

object ScalaSource extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("ScalaSource", _namePackage = Seq ("draco", "scalasource")))
  lazy val dracoType: Type[ScalaSource] = Type[ScalaSource] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[ScalaSource] = Domain[ScalaSource] (typeDefinition)
  lazy val generator: TypeDefinition => String = Generator.generate
}
