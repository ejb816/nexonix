package draco.base

import draco.Primal

trait Nominal extends Unit with Primal[String] {
  override val name: String = "Nominal"
  override val description: String = "Sequence of glyphs signifying name or identity"
}

object Nominal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Nominal",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Unit", _namePackage = Seq ("draco", "base")),
      draco.TypeName ("Primal[String]", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      draco.Fixed ("name", "String"),
      draco.Fixed ("description", "String")
    )
  )
  lazy val typeInstance: draco.Type[Nominal] = draco.Type[Nominal] (typeDefinition)
}
