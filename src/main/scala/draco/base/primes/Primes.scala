package draco.base.primes

import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}

import scala.collection.immutable.Seq

trait Primes {
  val naturalSequence: Seq[Int]
  val primeSequence: Seq[Int]
  val compositeSequence: Seq[Int]
 }

object Primes extends App {
  def filter(naturals: LazyList[Int]): LazyList[Int] = {
    val p = naturals.head
    // The #:: operator constructs a LazyList (like : in Haskell)
    // We remove all multiples of p from the remainder, then recurse
    p #:: filter(naturals.tail.filter(_ % p != 0))
  }

  def naturals(start: Int = 0, step: Int = 1): LazyList[Int] = LazyList.from(start, step)

  def composites(primes: Seq[Int]): Seq[Int] = {
    require(primes.length >= 2, "The input sequence must contain at least two primes.")
    val isComposite: Int => Boolean = n => if (n < 2) false else !(2 to math.sqrt(n).toInt).exists(n % _ == 0)

    primes.sliding(2).flatMap {
      case Seq(p1, p2) =>
        (p1 + 1 until p2).filter(isComposite)
    }.toSeq
  }

  // List of n primes
  def primes(n: Int): Seq[Int] = {
    // Convert the first n primes to a List
    filter(naturals(2)).take(n)
  }

  def apply(n: Int): Primes = new Primes {
    val primeSequence: Seq[Int] = primes(n)
    val naturalSequence: Seq[Int] = naturals().take(primeSequence.last + 1)
    val compositeSequence: Seq[Int] = composites(primeSequence)
  }

  lazy implicit val encoder: Encoder[Primes] = Encoder.instance { ps =>
    Json.obj (
      "naturalSequence" -> ps.naturalSequence.asJson,
      "primeSequence" -> ps.primeSequence.asJson,
      "compositeSequence" -> ps.compositeSequence.asJson
    )
  }
}
