
package draco.primes.rules

import org.evrete.api.Knowledge
import org.evrete.api.RhsContext
import draco.primes.Accumulator

trait RemoveFromSequence extends draco.Rule

object RemoveFromSequence {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("RemoveFromSequence")
    .forEach (
		"$accumulator", classOf[Accumulator],
		"$i1", classOf[Int],
		"$i2", classOf[Int],
		"$i3", classOf[Int]
    )
    .where("$i1 * $i2 == $i3")
    .execute ((context: RhsContext) => {
    	val accumulator: Accumulator = context.get[Accumulator]("$accumulator")
		val i1: Int = context.get[Int]("$i1")
		val i2: Int = context.get[Int]("$i2")
		val i3: Int = context.get[Int]("$i3")
		val newText: String = s"Composite number $i3 removed -> $i1 * $i2 == $i3"
		context.delete(i3)
		accumulator.primeSet.remove(i3)
		accumulator.compositeSet.addOne(i3)
		accumulator.intervalTextSet.addOne((System.nanoTime(), newText))
    })
    .build()
  }
}
