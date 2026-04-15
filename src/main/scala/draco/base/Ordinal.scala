package draco.base

import draco.Primal

trait Ordinal extends Unit with Primal[Enumeration] {
  override val name: String = "Ordinal"
  override val description: String = "Values associated with an ordered sequence"
}

object Ordinal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Ordinal", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Ordinal] = draco.Type[Ordinal] (typeDefinition)
}
