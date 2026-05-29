package draco.primes

import draco._

trait Numbers extends Primes {
  val primeSequence: Seq[Int]
  val naturalSequence: Seq[Int]
  val compositeSequence: Seq[Int]
}

object Numbers extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Numbers", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[Numbers] = Type[Numbers] (typeDefinition)
  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)

  def apply (
    _n: Int = 22
  ) : Numbers = new Numbers {
    override lazy val primeSequence: Seq[Int] = Primes.nPrimes(_n)
    override lazy val naturalSequence: Seq[Int] = Primes.naturals(2).take(primeSequence.last - 1)
    override lazy val compositeSequence: Seq[Int] = Primes.composites(primeSequence)
  }

  lazy val Null: Numbers = apply()

}
