package draco

import org.evrete.api.Knowledge

trait RuleSet {
  val knowledge: Knowledge
  val rules: Seq[Rule]

  def load(): Unit = rules.foreach(rule => rule(knowledge))
}
