package draco.base

trait Radians extends Rotation[Double] {
  override val name: String = "Radians"
  override val description: String = "Arc length divided by radius"
}

object Radians extends App {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Radians", _namePackage = Seq("draco", "base")))
  lazy val dracoType: draco.Type[Radians] = draco.Type[Radians] (typeDefinition)

  def apply (_value: Double) : Radians = new Radians {
    override val value: Double = _value
    override val typeDefinition: draco.TypeDefinition = Radians.typeDefinition
  }
}
