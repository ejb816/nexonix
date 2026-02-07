package draco

import org.nexonix.domains.Dictionary

trait DomainDictionary extends Dictionary[DomainType,TypeDictionary] {}

object DomainDictionary {
  def apply(
             _domains: Seq[DomainType] = Seq()
           ) : DomainDictionary = new DomainDictionary {
    override val kvMap: Map[DomainType, TypeDictionary] = _domains.map(domain => (domain, domain.typeDictionary)).toMap
  }
}