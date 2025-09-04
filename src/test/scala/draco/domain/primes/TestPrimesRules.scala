package draco.domain.primes

import draco.{Generator, RuleDefinition, SourceContent, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.evrete.api.{FactHandle, RhsContext, StatefulSession}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

class TestPrimesRules extends AnyFunSuite {

  test("Generate AddSequence") {
    val resourceClass = this.getClass
    val resourcePath = "/draco/domain/primes/AddSequence.json"
    val sourceContent = SourceContent(resourcePath, resourceClass)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: RuleDefinition = jsonContent.as[RuleDefinition].getOrElse(null)
    val ruleSource = Generator.generate(rule, Seq("draco", "domain", "primes"), Seq[TypeName]())
    println(ruleSource)
  }
  test("Generate RemoveFromSequence") {
    val resourceClass = this.getClass
    val resourcePath = "/draco/domain/primes/RemoveFromSequence.json"
    val sourceContent = SourceContent(_resourcePath = resourcePath, _resourceClass = resourceClass)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: RuleDefinition = jsonContent.as[RuleDefinition].getOrElse(null)
    val ruleSource = Generator.generate(rule, Seq("draco", "domain", "primes"), Seq[TypeName]())
    println(ruleSource)
  }
  test("FindPrimes") {

  }
  test("PrimesLessThanN") {
    val service: KnowledgeService = new KnowledgeService()
    // Create a Knowledge instance
    val knowledge = service.newKnowledge("Test Evrete")
      .builder()
      .newRule("prime numbers")
      .forEach(
        "$s1", classOf[String],
        "$i1", classOf[Integer],
        "$i2", classOf[Integer],
        "$i3", classOf[Integer])
      .where("$i1 * $i2 == $i3")
      .where("$s1.length > -1")
      .execute((ctx: RhsContext) => {
        val i1 = ctx.get[Int]("$i1")
        val i2 = ctx.get[Int]("$i2")
        val i3 = ctx.get[Int]("$i3")
        ctx.delete(i3)
        ctx.insert(s" $i1 * $i2 == $i3")
      })
      .build()
    // Stateful sessions are AutoCloseable
    try {
      val session: StatefulSession = knowledge.newStatefulSession()
      try { // Inject candidates and initialize
        val numbers = Primes.naturals(2).take(98)
        print(s"Input sequence (N = ${numbers.last}):\n$numbers\n")
        session.insert(numbers: _*)
        session.insert(Seq(""): _*)
        // Execute rules
        session.fire
        val collectedInt = ListBuffer[Int]()
        val collectdString = ListBuffer[String]()
        // Print current memory state
        session.forEachFact((fh: FactHandle, o: Any) => o match {
          case i: Int =>
            collectedInt += i
          case s: String =>
            collectdString += s
        })
        println(s"List of rules fired:${collectdString.mkString("\n")}")
        println(s"Number of rules fired: ${collectdString.length}")
        println(s"List of primeSequence ${collectedInt.sorted.toString().substring("ListBuffer".length)}")
      } finally if (session != null) session.close()
    }
    service.shutdown()
  }
  test("DomainRulesTest") {

  }

}
