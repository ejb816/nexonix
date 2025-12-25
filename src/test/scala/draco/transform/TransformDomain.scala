package draco.transform

import draco.Domain

trait TransformDomain[SO,SI] {
  val sourceDomain: Domain[SO]
  val sinkDomain: Domain[SI]
}

object TransformDomain {}
