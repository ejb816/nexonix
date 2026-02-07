package draco.transform

import org.nexonix.domains.Domain

trait TransformDomain[SO,SI] {
  val sourceDomain: Domain[SO]
  val sinkDomain: Domain[SI]
}

object TransformDomain {}
