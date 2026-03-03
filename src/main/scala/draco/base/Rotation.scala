package draco.base

trait Rotation[T] extends Cardinal[T] {
  override val name: String = "Rotation"
  override val description: String = "Measure of arc of unit circle"
}

object Rotation extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Rotation[T]",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Cardinal[T]", _namePackage = Seq ("draco", "base"))
    ),
    _elements = Seq (
      draco.Fixed ("name", "String"),
      draco.Fixed ("description", "String")
    )
  )
  lazy val typeInstance: draco.Type[Rotation[_]] = draco.Type[Rotation[_]] (typeDefinition)
}
