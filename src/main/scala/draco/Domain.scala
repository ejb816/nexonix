package draco

trait Domain[T] extends DomainType

object Domain extends App with TypeInstance {
  // Provisional until type parameters are handled in TypeName
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Domain[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("DomainType", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[Domain[_]] = Type[Domain[_]] (typeDefinition)

  def apply[T] (
                 _domainName: DomainName
               ) : Domain[T] = new Domain[T] {
    override val domainName: DomainName = _domainName
    override val typeDefinition: TypeDefinition = TypeDefinition(domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
  }
}