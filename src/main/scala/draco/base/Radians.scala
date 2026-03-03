package draco.base

trait Radians extends Rotation[Double] {
  override val name: String = "Radians"
  override val description: String = "Arc length divided by radius"
  override val typeDefinition: draco.TypeDefinition = Radians.typeInstance.typeDefinition
}

object Radians extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Radians",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Rotation[Double]", _namePackage = Seq ("draco", "base"))
    ),
    _elements = Seq (
      draco.Fixed ("name", "String"),
      draco.Fixed ("description", "String")
    ),
    _factory = draco.Factory (
      "Radians",
      _parameters = Seq (
        draco.Parameter ("value", "Double", "")
      )
    )
  )
  lazy val typeInstance: draco.Type[Radians] = draco.Type[Radians] (typeDefinition)

  def apply (_value: Double) : Radians = new Radians {
    override val value: Double = _value
  }
}
