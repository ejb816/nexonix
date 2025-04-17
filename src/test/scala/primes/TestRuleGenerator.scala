package primes

import org.evrete.KnowledgeService
import org.scalatest.funsuite.AnyFunSuite

class TestRuleGenerator extends AnyFunSuite {
  val service = new KnowledgeService
  private def generateRules (ruleNames: Seq[String]) : Seq[Rule] = {
    Seq[Rule]()
  }
  test("FindPrimes") {
    val ruleSeq = Seq("AddSequence", "RemoveFromSequence")
    val knowledge = service.newKnowledge()

  }
}
