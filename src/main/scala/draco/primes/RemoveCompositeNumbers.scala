package draco.primes

import draco.Rule
import org.evrete.api.{Knowledge, RhsContext}

trait RemoveCompositeNumbers extends Rule

object RemoveCompositeNumbers {
  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
      .newRule("RemoveCompositeNumbers")
      .forEach(
        "$accumulator", classOf[Accumulator],
        "$i1", classOf[Integer],
        "$i2", classOf[Integer],
        "$i3", classOf[Integer]
      )
      .where("$i1 * $i2 == $i3")
      .execute((context: RhsContext) => {
        val accumulator: Accumulator = context.get[Accumulator]("$accumulator")
        val i1 = context.get[Integer]("$i1")
        val i2 = context.get[Integer]("$i2")
        val i3 = context.get[Integer]("$i3")
        context.delete(i3)
        val prefix: String = s"Composite number $i3 removed "
        val newText: String = s"$prefix -> $i1 * $i2 == $i3"
        accumulator.primeSet.remove(i3)
        accumulator.compositeSet.addOne(i3)
        accumulator.intervalTextSet.addOne((System.nanoTime(), newText))
      })
      .build()
  }
}
