package draco.primes

import draco.Rule
import org.evrete.api.{Knowledge, RhsContext}

import java.util

trait AddNaturalSequence extends Rule
object AddNaturalSequence {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
      .newRule ("AddNaturalSequence")
      .forEach (
        "$accumulator", classOf[Accumulator],
        "$i", classOf[Integer]
      )
      .execute ((ctx: RhsContext) => {
        val accumulator = ctx.get[Accumulator]("$accumulator")
        val i = ctx.get[Integer]("$i")
        accumulator.primeSet.addOne(i)
        accumulator.naturalSet.addOne(i)
        val text = s"Added $i to primeSet and naturalSet."
        accumulator.intervalTextSet.addOne((System.nanoTime(), text))
      })
      .build()
  }
}
