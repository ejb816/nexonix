package draco.domain.primes

import draco.domain.rule.DomainRule
import org.evrete.api.{Knowledge, RhsContext}

trait RemoveFromSequence extends DomainRule

object RemoveFromSequence {

  val rule: Knowledge => Unit = knowledge => {
    knowledge
      .builder()
      .newRule("RemoveFromSequence")
      .forEach("$fp", classOf[FindPrimes], "$i1", classOf[Int], "$i2", classOf[Int], "$i3", classOf[Int])
      .where("$i1 * $i2 == $i3")

      .execute((context: RhsContext) => {
        val fp: FindPrimes = context.get[FindPrimes]("$fp")
        val i1: Int = context.get[Int]("$i1")
        val i2: Int = context.get[Int]("$i2")
        val i3: Int = context.get[Int]("$i3")
        val baseCounter = fp.conditionalPrint(i3, s" -> $i1 * $i2 = $i3")
        context.delete(i3)
        context.delete(fp)
        context.insert(FindPrimes(fp.numberOfPrimes, fp.delta, baseCounter._1, baseCounter._2))
      })
      .build()
  }
}
