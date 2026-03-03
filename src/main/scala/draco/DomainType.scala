package draco

trait DomainType extends DracoType {
  val domainName: DomainName
  val typeDictionary: TypeDictionary
}

object DomainType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DomainType",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("DracoType", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      Fixed (
        _name = "domainName",
        _valueType = "DomainName"
      ),
      Fixed (
        _name = "typeDictionary",
        _valueType = "TypeDictionary"
      )
    )
  )
  lazy val typeInstance: Type[DomainType] = Type[DomainType] (typeDefinition)

  def apply (
            _domainName: DomainName,
            _typeDictionary: TypeDictionary = TypeDictionary.Null
            ) : DomainType = new DomainType {
    override val domainName: DomainName = _domainName
    override val typeDefinition: TypeDefinition = TypeDefinition(_domainName.typeName)
    override val typeDictionary: TypeDictionary = _typeDictionary
  }
}

