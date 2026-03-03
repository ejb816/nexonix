package draco.base

trait Distance[T] extends Cardinal[T]

object Distance extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Distance[T]",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Cardinal[T]", _namePackage = Seq ("draco", "base"))
    )
  )
  lazy val typeInstance: draco.Type[Distance[_]] = draco.Type[Distance[_]] (typeDefinition)
}