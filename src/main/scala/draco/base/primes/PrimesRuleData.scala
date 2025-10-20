package draco.base.primes

import org.evrete.api.Knowledge
 trait PrimesRuleData {
   val primes: Primes
   val textList: Seq[String] = Seq[String]()
 }

object PrimesRuleData {
  def apply(
             _primes: Primes,
             _textList: Seq[String] = Seq[String]()
           ): PrimesRuleData = {
    new PrimesRuleData {
      val primes: Primes = _primes
      override val textList: Seq[String] = _textList
    }
  }

  val rules: Seq[Knowledge => Unit] = Seq(
    AddSequence.rule,
    RemoveFromSequence.rule
  )
}
