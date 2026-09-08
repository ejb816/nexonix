package draco.gendrake

import draco._
import draco.generator._
import draco.draketarget._

trait GenDrake extends draco.Generator[DrakeTarget]

object GenDrake extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("GenDrake", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[GenDrake] = Type[GenDrake] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)
  lazy val generator: TypeDefinition => String = draco.Drake.emit
}
