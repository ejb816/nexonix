package draco

trait Type[T] extends DracoType

object Type extends App with TypeInstance {
  // Provisional until type parameters are handled in TypeName
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Type[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("DracoType", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[Type[_]] = Type[Type[_]] (typeDefinition)

  def apply[T] (
    _typeDefinition: TypeDefinition
  ) : Type[T] = new Type[T] {
    override val typeDefinition: TypeDefinition = _typeDefinition
  }
}