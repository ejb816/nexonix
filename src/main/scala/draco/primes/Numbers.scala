package draco.primes
import draco._

trait Numbers extends Primes {
  val primeSequence: Seq[Int]
  val naturalSequence: Seq[Int]
  val compositeSequence: Seq[Int]
}

object Numbers extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Numbers",
      _namePackage = Seq ("draco", "primes")
    ),
    _derivation = Seq (
      draco.TypeName ("Primes", _namePackage = Seq ("draco", "primes"))
    ),
    _elements = Seq (
      draco.Fixed ("primeSequence", "Seq[Int]"),
      draco.Fixed ("naturalSequence", "Seq[Int]"),
      draco.Fixed ("compositeSequence", "Seq[Int]")
    ),
    _factory = draco.Factory (
      "Numbers",
      _parameters = Seq (
        draco.Parameter ("n", "Int", "22")
      )
    )
  )
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