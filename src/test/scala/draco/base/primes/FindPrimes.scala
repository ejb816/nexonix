package draco.base.primes

import io.circe.{Decoder, Encoder, Json}
import org.evrete.api.Knowledge

sealed trait FindPrimes extends Primes {
  val numberOfPrimes = 25
  val countBase = 0
  val counter = 0
  val baseMax = 0
  val delta = 1
  val conditionalPrint: (Int, String) => (Int, Int) = (n, s) => {
    println (s + n)
    (0, 1)
  }
}

object FindPrimes {
  def apply(
             _numberOfPrimes: Int,
             _delta: Int,
             _base: Int = 0,
             _counter: Int = 0
           ): FindPrimes = {
    new FindPrimes {
      override val primeSequence: Seq[Int] = primes(_numberOfPrimes)
      override val compositeSequence: Seq[Int] = composites(primeSequence)
      override val naturalSequence: Seq[Int] = naturals().take(primeSequence.last)

      override val baseMax: Int = primeSequence.last * _delta
      override val delta: Int = _delta
      override val countBase: Int = _base
      override val conditionalPrint: (Int, String) => (Int, Int) = (p, s) => {
        val test1: Int => Boolean = p => p > countBase || p == baseMax
        val test2: Int => Boolean = p => p > baseMax
        val testP: Int => Boolean = p => (if (p == baseMax) test2 else test1)(p)
        val newCount = _counter + 1
        val newBase = if (testP(p)) {
          println(s"$countBase rule fire number $counter$s")
          countBase + delta
        } else countBase
        (newCount, newBase)
      }

      val value: Seq[Int] = primes(_numberOfPrimes)

      def apply(nth: Int): Int = value(nth - 1)
    }
  }

  val rules: Seq[Knowledge => Unit] = Seq(
    AddSequence.rule,
    RemoveFromSequence.rule
  )

  implicit val encoder: Encoder[FindPrimes] = Encoder.instance { fp =>
    Json.obj(
      "numberOfPrimes" -> Json.fromInt(fp.numberOfPrimes),
      "delta" -> Json.fromInt(fp.delta)
    )
  }

  implicit val decoder: Decoder[FindPrimes] = Decoder.instance { cursor =>
    cursor.downField("numberOfPrimes").as[Int]
      .flatMap(_numberOfPrimes =>
        cursor.downField("delta").as[Int]
          .map(_delta => FindPrimes(_numberOfPrimes, _delta)))
  }
}
