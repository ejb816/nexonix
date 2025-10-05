package draco

trait DomainType {
  val domainName: DomainName
  val typeDefinition: TypeDefinition
  val typeDictionary: TypeDictionary
  val subDomainNames: Seq[String]
}

object DomainType {
  def apply (_domainName: DomainName, _domainNames: Seq[String] = Seq ()) : DomainType = new DomainType {
    override val domainName: DomainName = _domainName
    override val typeDefinition: TypeDefinition = TypeDefinition.load(_domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary(_domainName)
    override val subDomainNames: Seq[String] = _domainNames
  }
}

