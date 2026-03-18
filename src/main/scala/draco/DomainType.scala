package draco

trait DomainType extends DracoType {
  val domainDefinition: DomainDefinition
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
        _name = "domainDefinition",
        _valueType = "DomainDefinition"
      ),
      Fixed (
        _name = "typeDictionary",
        _valueType = "TypeDictionary"
      )
    )
  )
  lazy val typeInstance: Type[DomainType] = Type[DomainType] (typeDefinition)
  lazy val Null: DomainType = new DomainType {
    override val domainDefinition: DomainDefinition = DomainDefinition.Null
    override val typeDictionary: TypeDictionary = TypeDictionary.Null
    override val typeDefinition: TypeDefinition = TypeDefinition.Null
  }

  def apply (
            _domainDefinition: DomainDefinition,
            _typeDictionary: TypeDictionary = TypeDictionary.Null
            ) : DomainType = new DomainType {
    override val domainDefinition: DomainDefinition = _domainDefinition
    override val typeDefinition: TypeDefinition = TypeDefinition(_domainDefinition.typeName)
    override val typeDictionary: TypeDictionary = _typeDictionary
  }
}
