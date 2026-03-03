package draco

trait DomainDictionary extends Dictionary[DomainType,TypeDictionary] {}

object DomainDictionary extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DomainDictionary",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("Dictionary[DomainType,TypeDictionary]", _namePackage = Seq ("org", "nexonix", "domains"))
    ),
    _factory = Factory (
      "DomainDictionary",
      _parameters = Seq (
        Parameter ("domains", "Seq[DomainType]", "Seq()")
      )
    )
  )
  lazy val typeInstance: Type[DomainDictionary] = Type[DomainDictionary] (typeDefinition)

  def apply(
             _domains: Seq[DomainType] = Seq()
           ) : DomainDictionary = new DomainDictionary {
    override val kvMap: Map[DomainType, TypeDictionary] = _domains.map(domain => (domain, domain.typeDictionary)).toMap
  }
}