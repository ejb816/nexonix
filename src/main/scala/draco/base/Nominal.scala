package draco.base

import draco.Primal

trait Nominal extends Unit with Primal[String] {
  override val name: String = "Nominal"
  override val description: String = "Sequence of glyphs signifying name or identity"
}

object Nominal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Nominal", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Nominal] = draco.Type[Nominal] (typeDefinition)
}
