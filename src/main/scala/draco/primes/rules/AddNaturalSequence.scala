
package draco.primes.rules

import org.evrete.api.Knowledge
import org.evrete.api.RhsContext
import draco.primes.Accumulator

trait AddNaturalSequence extends draco.Rule

object AddNaturalSequence {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("AddNaturalSequence")
    .forEach (
		"$accumulator", classOf[Accumulator],
		"$i", classOf[Integer]
    )
    
    .execute ((context: RhsContext) => {
    	val accumulator: Accumulator = context.get[Accumulator]("$accumulator")
		val i: Integer = context.get[Integer]("$i")
		accumulator.primeSet.addOne(i)
		accumulator.naturalSet.addOne(i)
		val text = s"Added $i to primeSet and naturalSet."
		accumulator.intervalTextSet.addOne((System.nanoTime(), text))
    })
    .build()
  }
}
