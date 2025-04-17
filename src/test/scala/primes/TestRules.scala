package primes

import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.evrete.api.FactBuilder.fact
import org.evrete.api.{Knowledge, RhsContext, StatefulSession}
import org.scalatest.funsuite.AnyFunSuite



class TestRules extends AnyFunSuite {

  def jsonContent(name: String) : String = {
    val jsonContent = SourceContent(name + ".json", classOf[SourceContent])
    jsonContent.sourceString
  }

  def convertJSONToYAML(name: String): String = {
    val jsonRuleDef = RuleBuilder.decodeUpdateRule(jsonContent(name))
    encodeRuleYAML(jsonRuleDef)
  }

  val service = new KnowledgeService

  def initialize(knowledge: Knowledge, init: StatefulSession => Unit): Unit = {
    val session = knowledge.newStatefulSession()
    try {
      init(session)
    } finally if (session != null) session.close()
  }

  test("FindPrimesLessThanBaseMax") {
    val knowledge = service
      .newKnowledge()
      .newRule("prime numbers < base maximum")
      .forEach(
        fact("$fp", classOf[FindPrimes]),
        fact("$i1", classOf[Integer]),
        fact("$i2", classOf[Integer]),
        fact("$i3", classOf[Integer])
      )
      .where("$i1 * $i2 == $i3")
      .execute((ctx: RhsContext) => {
        val fp = ctx.get[FindPrimes]("$fp")
        val i1 = ctx.get[Int]("$i1")
        val i2 = ctx.get[Int]("$i2")
        val i3 = ctx.get[Int]("$i3")
        fp.conditionalPrint(i3, " -> " + i1 + " * " + i2 + " = " + i3)
        ctx.deleteFact("$i3")
      })

    // Stateful sessions are AutoCloseable
    initialize(knowledge, session => {
      val fp = FindPrimes.define(session, 100, 1, 0, 0)
      session.insert(fp)
      for (i <- 2 to fp.baseMax) {
        session.insert(i)
      }
      // Execute rules
      session.fire
      // Print current memory state
      session.forEachFact((o: Any) => print(o + " "))
    })
  }

  test("LessThanMaximum") {
    val knowledge = service.newKnowledge()
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
        fp.conditionalPrint(i3, " -> " + i1 + " * " + i2 + " = " + i3)
        context.delete(i3)
      })
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
    // Stateful sessions are AutoCloseable
    initialize(knowledge, session => {
      val fp = FindPrimes.define(session, 25, 5, 0, 0)
      session.insertAndFire(fp)
      session.forEachFact((o: Any) => {
        if (o.getClass.equals(classOf[Integer])) print(o + " ")
        else print("Primes found ")
      })
    })
  }


  test("TestAddSequence") {
    val ruleSet = Seq("/draco/domain/primes/TestAddSequence")
    val knowledge = service.newKnowledge()
    ruleSet.foreach(ruleName => {
      println(ruleName + ":")
      val ruleData = jsonContent(ruleName)
      val ruleDef = decodeUpdateRule(ruleData)
      //generateRule(ruleDef, knowledge)
    })

    initialize(knowledge, session => {
      val jsonString =
        """{
          |  "delta": 1,
          |  "maximum": 25
          |}""".stripMargin
      val fp: Json = parser.parse(jsonString).getOrElse(Json.Null)
//      session.insert(fp)
      // Execute rules
      session.fire
      // Print current memory state
      session.forEachFact((o: Any) => System.out.print(o + " "))
    })
  }

  test("LessThanBaseMax") {
    val ruleSet = Seq("/draco/domain/primes/LessThanBaseMax")
    val knowledge = service.newKnowledge()
    ruleSet.foreach(ruleName => {
      println(ruleName + ":")
      val ruleData = jsonContent(ruleName)
      val ruleDef = decodeUpdateRule(ruleData)
      //generateRule(ruleDef, knowledge)
    })

    initialize(knowledge, session => {
      val fp = FindPrimes.define(session, 100, 1, 0, 0)
//      session.insert(fp)
//      for (i <- 2 to fp.baseMax) {
//        session.insert(i)
//      }
      // Execute rules
      session.fire
      // Print current memory state
      session.forEachFact((o: Any) => System.out.print(o + " "))
    })
  }

  test("FindPrimes") {
    val ruleSet = Seq(
      "/draco/domain/primes/AddSequence",
      "/draco/domain/primes/RemoveFromSequence"
    )
    val knowledge = service.newKnowledge()
    ruleSet.foreach(ruleName => {
      println(ruleName + ":")
      val ruleData = jsonContent(ruleName)
      val ruleDef = decodeUpdateRule(ruleData)
      //generateRule(ruleDef, knowledge)
    })
    initialize(knowledge, session => {
      val fpJSON =
        """{
          |  "delta": 2,
          |  "maximum": 100
          |}""".stripMargin
      val fp: Json = parser.parse(fpJSON).getOrElse(Json.Null)
      session.insertAndFire(fp)
      session.forEachFact((o: Any) => print(o + " "))
    })
 }
}
