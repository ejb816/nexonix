package draco

import org.evrete.api.Knowledge

trait Rule {
  val rule: Knowledge => Unit
}

object Rule {
  def apply (_rule: Knowledge => Unit): Unit = {
    val rule: Knowledge => Unit = _rule
  }
}