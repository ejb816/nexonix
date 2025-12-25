package draco

trait Domain [T] extends DomainType {}

object Domain {
  def apply[T] (
                 _domainName: DomainName,
                 _domains: Seq[DomainType] = Seq ()
               ) : Domain[T] = new Domain[T] {
    override val domainName: DomainName = _domainName
    override val typeDefinition: TypeDefinition = TypeDefinition(domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = _domains
    override val domainDictionary: DomainDictionary = DomainDictionary(domains)
  }
}