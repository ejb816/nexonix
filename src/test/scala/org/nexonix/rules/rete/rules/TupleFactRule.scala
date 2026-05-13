
package org.nexonix.rules.rete.rules

import draco._
import org.nexonix.rules.rete._
import org.nexonix.rules._
import org.nexonix._
import org._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait TupleFactRule

object TupleFactRule extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadRuleType(TypeName ("TupleFact", _namePackage = Seq ("org", "nexonix", "rules", "rete", "rules")))
  lazy val dracoType: Type[TupleFactRule] = Type[TupleFactRule] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {

  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("org.nexonix.rules.rete.rules.TupleFact.rule")
    .forEach (

    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[TupleFactRule] (
    typeDefinition,
    _pattern = pattern,
    _action = action
  )
}
