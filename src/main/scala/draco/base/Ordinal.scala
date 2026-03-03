package draco.base

import draco.Primal

trait Ordinal extends Unit with Primal[Enumeration] {
  override val name: String = "Ordinal"
  override val description: String = "Values associated with an ordered sequence"
}

object Ordinal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Ordinal",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Unit", _namePackage = Seq ("draco", "base")),
      draco.TypeName ("Primal[Enumeration]", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      draco.Fixed ("name", "String"),
      draco.Fixed ("description", "String")
    )
  )
  lazy val typeInstance: draco.Type[Ordinal] = draco.Type[Ordinal] (typeDefinition)
}
