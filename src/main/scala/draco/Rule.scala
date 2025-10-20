package draco

import org.evrete.api.Knowledge

trait Rule {
  val rule: Knowledge => Unit
  def apply(_knowledge: Knowledge): Unit = rule
}
