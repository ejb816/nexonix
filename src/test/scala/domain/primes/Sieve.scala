package domain.primes

object Sieve {
  // Basic Sieve of Eratosthenes prime filter (lazy evaluation)
  def primeFilter(numbers: LazyList[Int]): LazyList[Int] = {
    val p = numbers.head
    // The #:: operator constructs a LazyList (like : in Haskell)
    // We remove all multiples of p from the remainder, then recurse
    p #:: primeFilter(numbers.tail.filter(_ % p != 0))
  }

  // Infinite stream of primes starting at 2
  val primesTo: LazyList[Int] = primeFilter(LazyList.from(2))

  // List of n primes
  def primes(n: Int): List[Int] = {
    // Convert the first n primes to a List
    primesTo.take(n).toList
  }

  // nth prime
  def prime(n: Int): Int = {
    // The last element of the list of n primes
    primes(n).last
  }

  // Small demonstration: print the first 10 primes and the 10th prime
  def main(args: Array[String]): Unit = {
    val first10Primes = primes(10)
    println(s"First 10 primes: $first10Primes")
    println(s"10th prime: ${prime(10)}")
  }
}
