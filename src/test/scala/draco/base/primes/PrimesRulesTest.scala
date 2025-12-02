package draco.base.primes

import draco.primes.{AddSequence, Primes, PrimesRuleData, RemoveFromSequence}
import draco.{Generator, RuleDefinition, SourceContent, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.evrete.api.{FactHandle, Knowledge, RhsContext, StatefulSession}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

class PrimesRulesTest extends AnyFunSuite {
  test("Generate AddSequence") {
    val resourceClass = this.getClass
    val resourcePath = "/draco/base/primes/AddSequence.json"
    val sourceContent = SourceContent(_resourcePath = resourcePath, _resourceClass = resourceClass)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: RuleDefinition = jsonContent.as[RuleDefinition].getOrElse(null)
    val ruleSource = Generator.generate (rule, Seq("draco", "base", "primes"), Seq[TypeName]())
    println(ruleSource)
  }
  test("Generate RemoveFromSequence") {
    val resourceClass = this.getClass
    val resourcePath = "/draco/base/primes/RemoveFromSequence.json"
    val sourceContent = SourceContent(_resourcePath = resourcePath, _resourceClass = resourceClass)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: RuleDefinition = jsonContent.as[RuleDefinition].getOrElse(null)
    val ruleSource = Generator.generate (rule, Seq("draco", "base", "primes"), Seq[TypeName]())
    println(ruleSource)
  }
  test("PrimesLessThanMaximum") {
    val service: KnowledgeService = new KnowledgeService()
    // Create a Knowledge instance
    val knowledge = service.newKnowledge("PrimesLessThanMaximum")
      .builder()
      .newRule("prime numbers")
      .forEach(
        "$prd", classOf[PrimesRuleData],
        "$i1", classOf[Integer],
        "$i2", classOf[Integer],
        "$i3", classOf[Integer])
      .where("$i1 * $i2 == $i3")
      .where("$prd.textList.length > -1")
      .execute((ctx: RhsContext) => {
        val i1 = ctx.get[Int]("$i1")
        val i2 = ctx.get[Int]("$i2")
        val i3 = ctx.get[Int]("$i3")
        val prd = ctx.get[PrimesRuleData]("$prd")

        val text: Seq[String] = Seq (if (prd.textList.length < 1) "Starting..."
        else "") ++ Seq(s" -> $i3 == $i1 * $i2")

        ctx.delete(prd)
        ctx.insert(PrimesRuleData(prd.primes, text))
        ctx.delete(i3)
      })
      .build()
    // Stateful sessions are AutoCloseable
    try {
      val session: StatefulSession = knowledge.newStatefulSession()
      try { // Inject candidates
        val numbers = Primes.naturals(2).take(25)
        session.insert(numbers: _*)
        session.insert(Seq(PrimesRuleData(Primes(25))): _*)
        // Execute rules
        session.fire
        val collectedInt = ListBuffer[Int]()
        // Print current memory state
        session.forEachFact((fh: FactHandle, o: Any) => o match {
          case i: Int =>
            collectedInt += i
          case prd: PrimesRuleData =>
            println(prd.textList.mkString("\n"))
        })
        println(s"List of primeSequence ${collectedInt.sorted.toString().substring("ListBuffer".length)}")
      } finally if (session != null) session.close()
    }
    service.shutdown()
  }
  test("AltPrimesLessThanMaximum") {
    val service: KnowledgeService = new KnowledgeService()
//    val knowledge = service.newKnowledge("PrimesLessThanMaximum")
//      .builder()
//      .newRule("PrimeNumbers")
//      .forEach(
//        "$prd", classOf[PrimesRuleData],
//        "$i1", classOf[java.lang.Integer],
//        "$i2", classOf[java.lang.Integer],
//        "$i3", classOf[java.lang.Integer])
//
//      // Compiled predicates (no EL)
//      .where("$prd", (prd: PrimesRuleData) => (prd.textList.length >= 0): java.lang.Boolean)
//      .where("$i3",  (i3: java.lang.Integer) => (i3.intValue() > 1): java.lang.Boolean)
//      .where("$i1",  (i1: java.lang.Integer) => (i1.intValue() > 1): java.lang.Boolean)
//      .where("$i2",  (i2: java.lang.Integer) => (i2.intValue() > 1): java.lang.Boolean)
//
//      // Keep EL only for the 3-variable relation
//      .where("$i1 * $i2 == $i3")
//
//      .execute((ctx: RhsContext) => {
//        val i3  = ctx.get[java.lang.Integer]("$i3")     // boxed handle
//        val prd = ctx.get[PrimesRuleData]("$prd")
//        prd.addText(s" -> ${i3.intValue()} composite")
//        ctx.update(prd)                                  // mutate + update same instance
//        ctx.delete(i3)                                   // delete exact matched Integer
//      })
//      .build()
  }
  test("TypeRulesTest") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge: Knowledge = service.newKnowledge("TypeRulesTest")
    val prd: PrimesRuleData = PrimesRuleData (Primes (100))
    try {
      AddSequence.rule (knowledge)
      RemoveFromSequence.rule (knowledge)
      val session: StatefulSession = knowledge.newStatefulSession()
      try { // Inject candidates
        session.insert(Seq[Any] (prd, 0): _*)
        // Execute rules
        session.fire
        val collectedInt = ListBuffer[Int]()
        // Print current memory state
        session.forEachFact((fh: FactHandle, o: Any) => o match {
          case i: Int =>
            collectedInt += i
          case prd: PrimesRuleData =>
            println(prd.textList.mkString("\n"))
        })
        println(s"List of primes ${collectedInt.sorted.toString().substring("ListBuffer".length)}")
      } finally if (session != null) session.close()
    }
    service.shutdown()
  }
}
