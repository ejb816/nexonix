package dreams

import draco.DomainType

trait Transform {
  val source: DomainType
  val sink:  DomainType
}

object Transform {
  def apply(
             _source: DomainType,
             _sink: DomainType
           ) : Transform = new Transform {
    override val source: DomainType = _source
    override val sink: DomainType = _sink
  }
}
