package draco.primes
import draco.DomainType

trait Numbers extends Primes {
  val primeSequence: Seq[Int]
  val naturalSequence: Seq[Int]
  val compositeSequence: Seq[Int]
}

object Numbers {
  def apply (n: Int = 22): Numbers = new Numbers {
    override val primeSequence: Seq[Int] = Primes.nPrimes(n)
    override val naturalSequence: Seq[Int] = Primes.naturals(2).take(primeSequence.last-1)
    override val compositeSequence: Seq[Int] = Primes.composites(primeSequence)
    override val domain: DomainType = Primes.primes.domain
  }
}