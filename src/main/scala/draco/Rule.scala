package draco

import org.evrete.api.Knowledge

trait Rule extends (Knowledge => Unit) {
  val rule: Knowledge => Unit
}
