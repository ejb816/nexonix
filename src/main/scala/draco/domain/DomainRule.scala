package draco.domain

import org.evrete.api.Knowledge

trait DomainRule extends (Knowledge => Unit) {
  val rule: Knowledge => Unit
}
