package draco.domain.define

import draco.domain.{Base, Domain, Dictionary, TypeData}

trait DataModel extends Domain[TypeData]

object DataModel {
  def apply (elements: (String, Domain[TypeData])*) : DataModel = {
    new DataModel {
      override val superDomain: Domain[TypeData] = this
      override val subDomains: Dictionary[Domain[TypeData]] = Map(elements: _*)
      override val base: Domain[TypeData] = Base.base
    }
  }
}
