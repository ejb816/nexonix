package draco.primes

import draco.DomainType
import draco.primes.Primes.primes
import org.evrete.api.Knowledge
trait PrimesRuleData extends Primes {
  val numbers: Numbers
  val textData: String
}

object PrimesRuleData {
  def apply (
             _numbers: Numbers = Numbers(),
             _textData: String = ""
           ): PrimesRuleData = new PrimesRuleData {

    override val numbers: Numbers = _numbers
    override val textData: String = _textData
    override val domain: DomainType = primes.domain
  }

  val rules: Seq[Knowledge => Unit] = Seq(
    AddNaturalSequence.rule,
    RemoveCompositeNumbers.rule
  )
}
