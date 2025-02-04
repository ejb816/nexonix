package domain.core

import org.nexonix.core.KeyDataMap

trait DomainMap extends KeyDataMap[String, Domain]

object DomainMap {
  def define(elements: (String, Domain)*) : DomainMap = {
    new DomainMap {
      override protected val internalMap: Map[String, Domain] = Map(elements: _*)
    }
  }
}