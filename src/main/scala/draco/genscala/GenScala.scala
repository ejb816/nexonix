package draco.genscala

import draco._
import draco.generator._
import draco.scalatarget._

trait GenScala extends draco.Generator[ScalaTarget]

object GenScala extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("GenScala", _namePackage = Seq ("draco", "genscala")))
  lazy val dracoType: Type[GenScala] = Type[GenScala] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GenScala] = Domain[GenScala] (typeDefinition)
  lazy val generator: TypeDefinition => String = DracoGenerator.generate
}
