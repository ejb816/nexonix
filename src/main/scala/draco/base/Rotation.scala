package draco.base

trait Rotation[T] extends Cardinal[T] {
  override val name: String = "Rotation"
  override val description: String = "Measure of arc of unit circle"
}

object Rotation extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Rotation", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Rotation[_]] = draco.Type[Rotation[_]] (typeDefinition)
}
