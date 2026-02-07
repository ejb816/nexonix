
package draco.primes.rules

trait AddNaturalSequence extends draco.Rule

object AddNaturalSequence {

  val action: java.util.function.Consumer[org.evrete.api.RhsContext] = (ctx: org.evrete.api.RhsContext) => {
      val accumulator: draco.primes.Accumulator = ctx.get[draco.primes.Accumulator]("$accumulator")
      val i: java.lang.Integer = ctx.get[java.lang.Integer]("$i")
      accumulator.primeSet.addOne(i)
      accumulator.naturalSet.addOne(i)
      val text: String = s"Added $i to primeSet and naturalSet."
      accumulator.intervalTextSet.addOne((System.nanoTime(), text))
  }

  val pattern: org.evrete.api.Knowledge => Unit = knowledge => {
    knowledge
    .builder()
    .newRule ("draco.primes.rules.AddNaturalSequence")
    .forEach (
      "$accumulator", classOf[draco.primes.Accumulator],
      "$i", classOf[java.lang.Integer]
    )

    .execute (action)
    .build()
  }
}
