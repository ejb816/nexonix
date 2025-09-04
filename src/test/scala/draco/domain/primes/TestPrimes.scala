package draco.domain.primes

import org.scalatest.funsuite.AnyFunSuite

class TestPrimes extends AnyFunSuite {
  test("Primes to prime sequence size: #") {
    def printPrimes (sequenceSize: Int) : Unit = {
      println(s"Primes to sequence size($sequenceSize):\n${Primes.primes(sequenceSize).toList.last}")
    }
    val primes = Primes(11)
    primes.primeSequence.foreach(p => printPrimes(p))
  }
}
