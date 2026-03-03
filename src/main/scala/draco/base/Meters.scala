package draco.base

trait Meters extends Distance[Double] {
  override val name: String = "Meters"
  override val description: String = "Distance or length measure"
  override val typeDefinition: draco.TypeDefinition = Meters.typeInstance.typeDefinition
}

object Meters extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Meters",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Distance[Double]", _namePackage = Seq ("draco", "base"))
    ),
    _elements = Seq (
      draco.Fixed ("name", "String"),
      draco.Fixed ("description", "String")
    ),
    _factory = draco.Factory (
      "Meters",
      _parameters = Seq (
        draco.Parameter ("value", "Double", "")
      )
    )
  )
  lazy val typeInstance: draco.Type[Meters] = draco.Type[Meters] (typeDefinition)

  def apply (_value: Double) : Meters = new Meters {
    override val value: Double = _value
  }
}
