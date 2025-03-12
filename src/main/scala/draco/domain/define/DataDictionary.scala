package draco.domain.define

import draco.domain.{Dictionary, TypeData}

sealed trait DataDictionary extends Dictionary[TypeData]

object DataDictionary {
  def apply (elements: (String, TypeData)*) : DataDictionary = {
    new DataDictionary {
      override protected val internalMap: Map[String, TypeData] = Map(elements: _*)
    }
  }
}