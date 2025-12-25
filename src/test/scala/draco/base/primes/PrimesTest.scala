package draco.base.primes

import draco.primes.Numbers
import org.scalatest.funsuite.AnyFunSuite
class PrimesTest extends AnyFunSuite {
  test("Primes") {
    // Small demonstration: print the first 22 primeSequence and the 22nd prime
    val numbers: Numbers = Numbers()
    val primes = numbers.primeSequence.toList
    val naturals = numbers.naturalSequence.toList
    val composites = numbers.compositeSequence.toList
    println(s"First 22 primes: $primes")
    println(s"22nd prime: ${primes.last}")
    println(s"Naturals for 22 primes: $naturals")
    println(s"Composites between first 22 primes: $composites")
  }
}
