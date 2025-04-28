package draco.domain.rule

import org.evrete.api.Knowledge

trait DomainRule extends (Knowledge => Unit) {
  val rule: Knowledge => Unit
}
