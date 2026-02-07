
package org.nexonix.rete

import org.evrete.api.Knowledge
import org.evrete.api.RhsContext


trait TupleFactRule extends draco.Rule

object TupleFactRule {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("TupleFactRule")
    .forEach (
		"$fact", classOf[(Int,Int,Int)]
    )
    .where("$fact._1.equals(1) && $fact._2.equals(2) && $fact._3.equals(3)")
    .execute ((context: RhsContext) => {
    	val fact: (Int,Int,Int) = context.get[(Int,Int,Int)]("$fact")
		println (fact)
		println ((fact._1,fact._2,fact._3))
    })
    .build()
  }
}
