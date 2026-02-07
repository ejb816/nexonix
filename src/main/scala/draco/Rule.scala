package draco

import org.evrete.api.{Knowledge, RhsContext}

trait Rule {
  val pattern: Knowledge => Unit
  val action: RhsContext => Unit
}

object Rule {
  def apply (
      _pattern: Knowledge => Unit,
      _action: RhsContext => Unit
  ): Rule = new Rule {
    override val pattern: Knowledge => Unit = _pattern
    override val action: RhsContext => Unit = _action
  }
}