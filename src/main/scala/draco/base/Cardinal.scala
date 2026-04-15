package draco.base

import draco.Primal

trait Cardinal[T] extends Unit with Primal[T] {
  override val name: String = "Cardinal"
  override val description: String = "Atomic primitive or reference value"
}

object Cardinal extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Cardinal", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Cardinal[_]] = draco.Type[Cardinal[_]] (typeDefinition)
}

