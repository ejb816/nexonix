package draco.base.primes

import org.scalatest.funsuite.AnyFunSuite

import Primes._
class PrimesTest extends AnyFunSuite {
  test("Primes") {
    // Small demonstration: print the first 22 primeSequence and the 22nd prime
    val first22Primes = Primes(22).primeSequence
    println(s"First 22 primeSequence: $first22Primes")
    println(s"22nd prime: ${first22Primes(21)}")
    val first22PrimesIntervals: Seq[Int] = composites(first22Primes)
    println(s"Interval between first 22 primes: $first22PrimesIntervals")
  }
}
