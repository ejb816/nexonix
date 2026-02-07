
package draco.primes.rules

trait PrimesFromNaturalSequence extends draco.Rule

object PrimesFromNaturalSequence {
  def w0(i1: Integer, i2: Integer, i3: Integer): Boolean = i1 * i2 == i3
  val action: java.util.function.Consumer[org.evrete.api.RhsContext] = (ctx: org.evrete.api.RhsContext) => {
      val accumulator: draco.primes.Accumulator = ctx.get[draco.primes.Accumulator]("$accumulator")
      val i1: Integer = ctx.get[Integer]("$i1")
      val i2: Integer = ctx.get[Integer]("$i2")
      val i3: Integer = ctx.get[Integer]("$i3")
      val newText: (Long, String) = (System.nanoTime(), s" Remove $i3 ->\t$i3 == $i1 * $i2")
      ctx.delete(i3)
      accumulator.primeSet.remove(i3)
      accumulator.compositeSet.addOne(i3)
      accumulator.intervalTextSet.addOne(newText)
  }

  val pattern: org.evrete.api.Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("draco.primes.rules.PrimesFromNaturalSequence")
    .forEach (
      "$accumulator", classOf[draco.primes.Accumulator],
      "$i1", classOf[Integer],
      "$i2", classOf[Integer],
      "$i3", classOf[Integer]
    )
    .where("draco.primes.rules.PrimesFromNaturalSequence.w0($i1, $i2, $i3)")
    .execute (action)
    .build()
  }
}
