package draco

trait DomainDictionary extends Draco with Dictionary[Domain,TypeDictionary] {}

object DomainDictionary {
  def apply(domains: Seq[Domain]) : DomainDictionary = new DomainDictionary {
    override val kvMap: Map[Domain, TypeDictionary] = domains.map(domain => (domain, domain.typeDictionary)).toMap
  }
}