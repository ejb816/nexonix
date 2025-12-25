package draco.primes

import draco.Rule
import org.evrete.api.{Knowledge, RhsContext}
trait PrimesFromNaturalSequence extends Rule
object PrimesFromNaturalSequence {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
        .newRule("PrimesFromNaturalSequence")
        .forEach(
          "$accumulator", classOf[Accumulator],
          "$i1", classOf[Integer],
          "$i2", classOf[Integer],
          "$i3", classOf[Integer])
        .where("$i1 * $i2 == $i3")
        .execute((ctx: RhsContext) => {
          val accumulator = ctx.get[Accumulator]("$accumulator")
          val i1 = ctx.get[Int]("$i1")
          val i2 = ctx.get[Int]("$i2")
          val i3 = ctx.get[Int]("$i3")
          val newText: (Long,String) = (System.nanoTime(),s" Remove $i3 ->\t$i3 == $i1 * $i2")
          ctx.delete(i3)
          accumulator.primeSet.remove(i3)
          accumulator.compositeSet.addOne(i3)
          accumulator.intervalTextSet.addOne(newText)
        })
      .build()
  }
}
