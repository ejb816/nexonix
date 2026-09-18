package draco.primes

import draco._

trait Primes extends DracoType {
  lazy val knowledge: org.evrete.api.Knowledge = Rule.knowledgeService.newKnowledge("Primes")
}

object Primes extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Primes", _namePackage = Seq ("draco", "primes")))
  lazy val dracoType: Type[Primes] = Type[Primes] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Accumulator", "Numbers", "AddNaturalSequence", "PrimesFromNaturalSequence", "RemoveCompositeNumbers")

  lazy val domainType: Domain[Primes] = Domain[Primes] (typeDefinition)
  def filter(_naturals: => LazyList[Int]): LazyList[Int] = {
    lazy val naturals: LazyList[Int] = _naturals
    lazy val p: Int = naturals.head
    p #:: filter(naturals.tail.filter(_ % p != 0))
  }
  def naturals(_start: => Int = 0, _step: => Int = 1): LazyList[Int] = {
    lazy val start: Int = _start
    lazy val step: Int = _step
    LazyList.from(start, step)
  }
  def composites(_primes: => Seq[Int]): Seq[Int] = {
    lazy val primes: Seq[Int] = _primes
    naturals(2).filterNot(primes.contains(_)).take(primes.last - 1)
  }
  def primesFromComposites(_composites: => Seq[Int]): Seq[Int] = {
    lazy val composites: Seq[Int] = _composites
    naturals(2).filterNot(composites.contains(_)).take(composites.last - composites.length)
  }
  def nPrimes(_n: => Int): Seq[Int] = {
    lazy val n: Int = _n
    filter(naturals(2)).take(n)
  }
}
