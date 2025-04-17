package draco.domain.primes

import io.circe.Json
import org.evrete.KnowledgeService
import org.evrete.api.FactBuilder.fact
import org.evrete.api.{Knowledge, RhsContext, StatefulSession}

sealed trait FindPrimes {
  val countBase = 0
  val counter = 0
  val baseMax = 0
  val maximum = 25
  val delta = 1
  val conditionalPrint: (Int, String) => (Int, Int) = (n, s) => {
    println (s + n)
    (0, 1)
  }
}

object FindPrimes {
  val service: KnowledgeService = new KnowledgeService()
  def define: (
      StatefulSession,
      Integer,
      Integer,
      Integer,
      Integer
    ) => FindPrimes =
    (
      _session,
      _maximum,
      _delta,
      _base,
      _counter
    ) => {
    new FindPrimes {
      override val baseMax: Int = _maximum * _delta
      override val maximum: Int = _maximum
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
      val knowledge: Knowledge = DomainRuleSet.service.newKnowledge()
      knowledge.newRule("Find prime numbers - add sequence")
        .forEach(
          fact("$fp", classOf[FindPrimes])
        )
        .execute((context: RhsContext) => {
          val fp = context.get[FindPrimes]("$fp")
          println("Adding input sequence of integers (2 to " + fp.maximum + "):")
          for (i <- 2 to fp.maximum) {
            print(" " + i)
            context.insert(i)
          }
          println("\n... to working memory")
          println()
        })
      knowledge.newRule("Find prime numbers - remove from sequence")
        .forEach(
          fact("$fp", classOf[FindPrimes]),
          fact("$i1", classOf[Integer]),
          fact("$i2", classOf[Integer]),
          fact("$i3", classOf[Integer])
        )
        .where("$i1 * $i2 == $i3")
        .execute((context: RhsContext) => {
          val fp = context.get[FindPrimes]("$fp")
          val i1 = context.get[Int]("$i1")
          val i2 = context.get[Int]("$i2")
          val i3 = context.get[Int]("$i3")
          val baseCounter = fp.conditionalPrint(i3, " -> " + i1 + " * " + i2 + " = " + i3)
          context.insert(
            FindPrimes.define(_session, fp.maximum, fp.delta, baseCounter._1, baseCounter._2)
          )
          context.delete(i3)
        })
    }
  }

  def run (data: Json): Unit = {

  }
}
