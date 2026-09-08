package draco.gendrake

import draco._
import draco.draketarget._

trait Emission extends Surface

object Emission extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Emission", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[Emission] = Type[Emission] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  def apply (
    _definition: TypeDefinition
  ) : Emission = new Emission {
    override lazy val value: String = draco.Drake.emit(_definition)
    override lazy val typeDefinition: TypeDefinition = Emission.typeDefinition
  }

  lazy val Null: Emission = apply(
    _definition = null.asInstanceOf[TypeDefinition]
  )


}
