
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
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("TupleFact", _namePackage = Seq ("org", "nexonix", "rules", "rete", "rules")))
  lazy val dracoType: Type[TupleFactRule] = Type[TupleFactRule] (typeDefinition)
  def w0(fact: (Int,Int,Int)): Boolean = fact._1.equals(1) && fact._2.equals(2) && fact._3.equals(3)
  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val fact: (Int,Int,Int) = ctx.get[(Int,Int,Int)]("$fact")
      println(fact)
      println((fact._1, fact._2, fact._3))
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("org.nexonix.rules.rete.rules.TupleFact")
    .forEach (
      "$fact", classOf[(Int,Int,Int)]
    )
    .where("org.nexonix.rules.rete.rules.TupleFactRule.w0($fact)")
    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[TupleFactRule] (
    _pattern = pattern,
    _action = action
  )
}
