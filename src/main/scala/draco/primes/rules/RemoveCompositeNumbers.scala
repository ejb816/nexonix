
package draco.primes.rules

trait RemoveCompositeNumbers extends draco.Rule

object RemoveCompositeNumbers {
  def w0(i1: Int, i2: Int, i3: Int): Boolean = i1 * i2 == i3
  val action: java.util.function.Consumer[org.evrete.api.RhsContext] = (ctx: org.evrete.api.RhsContext) => {
      val accumulator: draco.primes.Accumulator = ctx.get[draco.primes.Accumulator]("$accumulator")
      val i1: Int = ctx.get[Int]("$i1")
      val i2: Int = ctx.get[Int]("$i2")
      val i3: Int = ctx.get[Int]("$i3")
      val newText: String = s"Composite number $i3 removed -> $i1 * $i2 == $i3"
      ctx.delete(i3)
      accumulator.primeSet.remove(i3)
      accumulator.compositeSet.addOne(i3)
      accumulator.intervalTextSet.addOne((System.nanoTime(), newText))
  }

  val pattern: org.evrete.api.Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("draco.primes.rules.RemoveCompositeNumbers")
    .forEach (
      "$accumulator", classOf[draco.primes.Accumulator],
      "$i1", classOf[Int],
      "$i2", classOf[Int],
      "$i3", classOf[Int]
    )
    .where("draco.primes.rules.RemoveCompositeNumbers.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }
}
