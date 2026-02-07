
package org.nexonix.rules.rete.rules

trait TupleFactRule extends draco.Rule

object TupleFactRule {
  def w0(fact: (Int,Int,Int)): Boolean = fact._1.equals(1) && fact._2.equals(2) && fact._3.equals(3)
  val action: java.util.function.Consumer[org.evrete.api.RhsContext] = (ctx: org.evrete.api.RhsContext) => {
      val fact: (Int,Int,Int) = ctx.get[(Int,Int,Int)]("$fact")
      println(fact)
      println((fact._1, fact._2, fact._3))
  }

  val pattern: org.evrete.api.Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("org.nexonix.rules.rete.rules.TupleFactRule")
    .forEach (
      "$fact", classOf[(Int,Int,Int)]
    )
    .where("org.nexonix.rules.rete.rules.TupleFactRule.w0($fact)")
    .execute (action)
    .build()
  }
}
