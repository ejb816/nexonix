package draco.base

trait Distance[T] extends Cardinal[T]

object Distance extends App {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Distance", _namePackage = Seq("draco", "base")))
  lazy val dracoType: draco.Type[Distance[_]] = draco.Type[Distance[_]] (typeDefinition)
}