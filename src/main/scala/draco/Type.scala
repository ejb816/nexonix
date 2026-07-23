package draco

trait Type[T] extends DracoType

object Type extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Type", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Type[_]] = Type[Type[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply[T] (
    _typeDefinition: TypeDefinition
  ) : Type[T] = new Type[T] {
    override lazy val typeDefinition: TypeDefinition = _typeDefinition
  }

  lazy val Null: Type[_] = apply[Nothing](
    _typeDefinition = null.asInstanceOf[TypeDefinition]
  )

}
