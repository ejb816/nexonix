package draco.base

trait Radians extends Rotation[Double] {
  override val name: String = "Radians"
  override val description: String = "Arc length divided by radius"
  override val typeInstance: draco.DracoType = Radians.typeInstance
  override val typeDefinition: draco.TypeDefinition = Radians.typeInstance.typeDefinition
}

object Radians extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Radians", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Radians] = draco.Type[Radians] (typeDefinition)

  def apply (_value: Double) : Radians = new Radians {
    override val value: Double = _value
  }
}
