package draco.primes

import draco.{DomainElement, DomainName, TypeName}
import org.evrete.KnowledgeService
import org.evrete.api.Knowledge
import org.nexonix.domains
import org.nexonix.domains.Domain

trait Primes extends DomainElement {
  override val knowledgeService: KnowledgeService = DomainElement.knowledgeService
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Primes")
}

object Primes {
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

  val primes: Primes = new Primes {
    override val domain: Domain[Primes] = domains.Domain[Primes] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "Primes",
          _namePackage = Seq ("draco", "primes")
        ),
        _elementTypeNames = Seq (
          "Numbers",
          "PrimesRuleData"
        )
      )
    )
  }
}
