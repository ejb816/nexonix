package draco.primes
import draco._

trait Numbers extends Primes {
  val primeSequence: Seq[Int]
  val naturalSequence: Seq[Int]
  val compositeSequence: Seq[Int]
}

object Numbers extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Numbers", _namePackage = Seq("draco", "primes")))
  lazy val typeInstance: draco.Type[Numbers] = Type[Numbers] (typeDefinition)

  def apply (n: Int = 22): Numbers = new Numbers {
    override val primeSequence: Seq[Int] = Primes.nPrimes(n)
    override val naturalSequence: Seq[Int] = Primes.naturals(2).take(primeSequence.last-1)
    override val compositeSequence: Seq[Int] = Primes.composites(primeSequence)
    override val domainInstance: DomainType = Primes.domainInstance
    override val typeDefinition: TypeDefinition = Numbers.typeDefinition
    override val typeInstance: DracoType = Numbers.typeInstance
  }
}