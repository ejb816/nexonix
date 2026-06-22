package draco.primes

import draco.PersistentTestLog
import draco.primes.Numbers
import org.scalatest.funsuite.AnyFunSuite
class PrimesTest extends AnyFunSuite with PersistentTestLog {
  test("Primes") {
    // Small demonstration: log the first 22 primeSequence and the 22nd prime
    val numbers: Numbers = Numbers()
    val primes = numbers.primeSequence.toList
    val naturals = numbers.naturalSequence.toList
    val composites = numbers.compositeSequence.toList
    log.info(s"First 22 primes: $primes")
    log.info(s"22nd prime: ${primes.last}")
    log.info(s"Naturals for 22 primes: $naturals")
    log.info(s"Composites between first 22 primes: $composites")
  }
}
