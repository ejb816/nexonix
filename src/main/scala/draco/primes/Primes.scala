package draco.primes

import draco._

trait Primes {
  lazy val knowledge: org.evrete.api.Knowledge = Rule.knowledgeService.newKnowledge("Primes")
}

object Primes extends App {
  lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Primes", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[Primes] = Type[Primes] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Accumulator", "Numbers", "AddNaturalSequence", "PrimesFromNaturalSequence", "RemoveCompositeNumbers")

  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)
  def filter(naturals: LazyList[Int]): LazyList[Int] = {
    val p: Int = naturals.head
    p #:: filter(naturals.tail.filter(_ % p != 0))
  }
  def naturals(start: Int = 0, step: Int = 1): LazyList[Int] = LazyList.from(start, step)
  def composites(primes: Seq[Int]): Seq[Int] = naturals(2).filterNot(primes.contains(_)).take(primes.last - 1)
  def primesFromComposites(composites: Seq[Int]): Seq[Int] = naturals(2).filterNot(composites.contains(_)).take(composites.last - composites.length)
  def nPrimes(n: Int): Seq[Int] = filter(naturals(2)).take(n)
}
