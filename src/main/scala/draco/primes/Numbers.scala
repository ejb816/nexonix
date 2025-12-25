package draco.primes
import draco.DomainType
import draco.primes.Primes.{composites, nPrimes, naturals, primes}

trait Numbers extends Primes {
  val primeSequence: Seq[Int]
  val naturalSequence: Seq[Int]
  val compositeSequence: Seq[Int]
}

object Numbers {
  def apply (n: Int = 22): Numbers = new Numbers {
    override val primeSequence: Seq[Int] = nPrimes(n)
    override val naturalSequence: Seq[Int] = naturals(2).take(primeSequence.last-1)
    override val compositeSequence: Seq[Int] = composites(primeSequence)
    override val domain: DomainType = primes.domain
  }
}