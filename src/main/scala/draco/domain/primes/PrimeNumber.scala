package draco.domain.primes

import draco.domain.primes.Primes.Sieve._

trait PrimeNumber {
  def ordinal: Int
  def cardinal: Int
}

object PrimeNumber {
  def define(n: Int) : PrimeNumber = {
    if (n < Primes.listOfPrimes.length) {
      new PrimeNumber {
        override def ordinal: Int = n
        override def cardinal: Int = Primes.listOfPrimes(n)
      }
    } else {
      Primes.listOfPrimes = primes(n)
      new PrimeNumber {
        override def ordinal: Int = n
        override def cardinal: Int = Primes.listOfPrimes(n)
      }
    }
  }
}