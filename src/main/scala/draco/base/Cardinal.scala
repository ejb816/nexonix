package draco.base

import draco.Primal

trait Cardinal[T] extends Unit with Primal[T] {
  override val name: String = "Cardinal"
  override val description: String = "Atomic primitive or reference value"
}

object Cardinal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Cardinal[T]",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Unit", _namePackage = Seq ("draco", "base")),
      draco.TypeName ("Primal[T]", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      draco.Fixed ("name", "String"),
      draco.Fixed ("description", "String")
    )
  )
  lazy val typeInstance: draco.Type[Cardinal[_]] = draco.Type[Cardinal[_]] (typeDefinition)
}

