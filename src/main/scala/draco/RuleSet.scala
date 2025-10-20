package draco

import org.evrete.api.Knowledge

trait RuleSet {
  def load (_knowledge: Knowledge = Draco.draco.knowledge): Unit = rules.foreach(rule => rule(_knowledge))
  val rules: Seq[Rule] = Seq ()
}

object RuleSet {
  def apply (
              _rules: Seq[Rule]
            ) : RuleSet = new RuleSet {
    override val rules: Seq[Rule] = _rules
  }
}
