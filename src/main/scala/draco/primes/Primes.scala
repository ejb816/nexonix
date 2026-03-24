package draco.primes

import draco._

trait Primes extends DomainInstance {
  val knowledge: org.evrete.api.Knowledge = Rule.knowledgeService.newKnowledge("Primes")
}

object Primes extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Primes",
      _namePackage = Seq ("draco", "primes")
    ),
    _derivation = Seq (
      TypeName ("DomainInstance", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[Primes] = Type[Primes] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Primes] {
    override lazy val domainDefinition: TypeDefinition = TypeDefinition (
      typeDefinition.typeName,
      _elementTypeNames = Seq (
        "Accumulator",
        "Numbers",
        "PrimeOrdinal"
      )
    )
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }

  def filter(naturals: LazyList[Int]): LazyList[Int] = {
    val p = naturals.head
    // The #:: operator constructs a LazyList (like : in Haskell)
    // We remove all multiples of p from the remainder, then recurse
    p #:: filter(naturals.tail.filter(_ % p != 0))
  }

  def naturals(start: Int = 0, step: Int = 1): LazyList[Int] = LazyList.from(start, step)

  def composites(primes: Seq[Int]): Seq[Int] = {
    naturals(2).filterNot(primes.contains(_)).take(primes.last - 1)
  }

  def primesFromComposites (composites: Seq[Int]) : Seq[Int] = {
    naturals(2).filterNot(composites.contains(_)).take(composites.last - composites.length)
  }
  // List of n primes
  def nPrimes(n: Int): Seq[Int] = {
    // Convert the first n primes to a List
    filter(naturals(2)).take(n)
  }
}
