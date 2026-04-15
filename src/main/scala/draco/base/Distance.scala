package draco.base

trait Distance[T] extends Cardinal[T]

object Distance extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Distance", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Distance[_]] = draco.Type[Distance[_]] (typeDefinition)
}