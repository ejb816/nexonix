package draco

trait Domain[T] extends DomainType

object Domain extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Domain", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Domain[_]] = Type[Domain[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply[T] (
    _domainDefinition: TypeDefinition
  ) : Domain[T] = new Domain[T] {
    override lazy val typeDefinition: TypeDefinition = _domainDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary(_domainDefinition)
  }

  lazy val Null: Domain[_] = apply[Nothing](
    _domainDefinition = null.asInstanceOf[TypeDefinition]
  )

}
