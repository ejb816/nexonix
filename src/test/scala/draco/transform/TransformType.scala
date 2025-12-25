package draco.transform

import draco.DomainType

trait TransformType  {
  val sourceType: DomainType
  val sinkType: DomainType
}

object TransformType {}