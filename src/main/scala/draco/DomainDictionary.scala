package draco

trait DomainDictionary extends Dictionary[DomainType,TypeDictionary] {}

object DomainDictionary {
  def apply(domains: Seq[DomainType]) : DomainDictionary = new DomainDictionary {
    override val kvMap: Map[DomainType, TypeDictionary] = domains.map(domain => (domain, domain.typeDictionary)).toMap
  }
}