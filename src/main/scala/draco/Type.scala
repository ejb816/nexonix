package draco

trait Type[T] extends DracoType

object Type extends App {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Type",
      _namePackage = Seq ("draco"),
      _typeParameters = Seq ("T")
    ),
    _derivation = Seq (
      TypeName ("DracoType", _namePackage = Seq ("draco"))
    )
  )
  lazy val dracoType: Type[Type[_]] = Type[Type[_]] (typeDefinition)

  def apply[T] (
    _typeDefinition: TypeDefinition
  ) : Type[T] = new Type[T] {
    override val typeDefinition: TypeDefinition = _typeDefinition
  }
}