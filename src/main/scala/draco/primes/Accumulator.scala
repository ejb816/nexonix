package draco.primes

import draco.{DomainType, TypeDefinition}

import scala.collection.mutable


trait Accumulator extends Primes {
  val primeSet: mutable.Set[Int] = mutable.Set[Int] ()
  val compositeSet: mutable.Set[Int] = mutable.Set[Int] ()
  val naturalSet: mutable.Set[Int] = mutable.Set[Int] ()
  val intervalTextSet: mutable.Set[(Long,String)] = mutable.Set[(Long,String)] ()
}

object Accumulator {
  def apply () : Accumulator = new Accumulator {
    override val domain: DomainType = Primes.primes.domain
  }
}
