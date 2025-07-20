package draco.domain

import draco.{Dictionary, RootType, TypeDictionary}

trait DomainDictionary extends Dictionary[Domain,TypeDictionary] {}

object DomainDictionary extends App {
  def apply(domainTypeNames: Seq[RootType]) : DomainDictionary = new DomainDictionary {
    override val kvMap: Map[Domain, TypeDictionary] = {
      domainTypeNames.map(dtn => (Domain(dtn), dtn.typeDictionary)).toMap
    }
  }
}