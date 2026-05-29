package draco.primes

import draco._

trait Primes {
  lazy val knowledge: org.evrete.api.Knowledge = Rule.knowledgeService.newKnowledge("Primes")
}

object Primes extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Primes", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[Primes] = Type[Primes] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Accumulator", "Numbers", "AddNaturalSequence.rule", "PrimesFromNaturalSequence.rule", "RemoveCompositeNumbers.rule")

  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)

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
