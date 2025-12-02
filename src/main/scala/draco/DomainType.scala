package draco

trait DomainType {
  val domainName: DomainName
  val typeDefinition: TypeDefinition
  val typeDictionary: TypeDictionary
  val domains: Seq[DomainType]
  val domainDictionary:  DomainDictionary
}

object DomainType {
  def apply (
            _domainName: DomainName,
            _typeDictionary: TypeDictionary = TypeDictionary.Null,
            _domains: Seq[DomainType] = Seq()
            ) : DomainType = new DomainType {
    override val domainName: DomainName = _domainName
    override val typeDefinition: TypeDefinition = TypeDefinition(_domainName.typeName)
    override val typeDictionary: TypeDictionary = _typeDictionary
    override val domains: Seq[DomainType] = _domains
    override val domainDictionary: DomainDictionary = DomainDictionary(domains)
  }
}

