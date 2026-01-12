
package draco.primes.rules

import org.evrete.api.Knowledge
import org.evrete.api.RhsContext
import draco.primes.Accumulator

trait PrimesFromNaturalSequence extends draco.Rule

object PrimesFromNaturalSequence {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("PrimesFromNaturalSequence")
    .forEach (
		"$accumulator", classOf[Accumulator],
		"$i1", classOf[Integer],
		"$i2", classOf[Integer],
		"$i3", classOf[Integer]
    )
    .where("$i1 * $i2 == $i3")
    .execute ((context: RhsContext) => {
    	val accumulator: Accumulator = context.get[Accumulator]("$accumulator")
		val i1: Integer = context.get[Integer]("$i1")
		val i2: Integer = context.get[Integer]("$i2")
		val i3: Integer = context.get[Integer]("$i3")
		val newText: (Long,String) = (System.nanoTime(),s" Remove $i3 ->\t$i3 == $i1 * $i2")
		context.delete(i3)
		accumulator.primeSet.remove(i3)
		accumulator.compositeSet.addOne(i3)
		accumulator.intervalTextSet.addOne(newText)
    })
    .build()
  }
}
