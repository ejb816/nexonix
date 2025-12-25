package draco.base.primes

import draco.primes.{Accumulator, AddNaturalSequence, Numbers, Primes, PrimesFromNaturalSequence, PrimesRuleData, RemoveCompositeNumbers}
import draco.{Generator, RuleDefinition, SourceContent, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.evrete.api.{FactHandle, Knowledge, RhsContext, StatefulSession}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Set}

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
  def printResult (accumulator: Accumulator, numbers: Numbers) : Unit = {
    // Print current memory state
    val sortedTextData: Seq[(Long,String)] = accumulator.intervalTextSet.toSeq.sortBy(_._1)
    val firstTime: Long = if (sortedTextData.nonEmpty) sortedTextData.head._1 else 0
    val timedText: Seq[String] = sortedTextData.map { case (n,s) => s"${n - firstTime} $s" }
    val primeList: List[Int] = if (accumulator.primeSet.isEmpty) {
      Primes.primesFromComposites(accumulator.compositeSet.toSeq).toList
    } else {
      accumulator.primeSet.toSeq.toList
    }
    val prefixLength: Int = "List".length
    println(s"Input Natural Sequence:${numbers.naturalSequence.toList.toString().substring(prefixLength)}")
    println(s"Result Prime Sequence: ${primeList.sorted.toString().substring(prefixLength)}")
    println(s"Result Composite Sequence: ${accumulator.compositeSet.toList.sorted.toString().substring(prefixLength)}")
    println(s"firstTime = $firstTime")
    println(s"Rule Results with Fire Interval:\n00000${timedText.mkString("\n")}")
  }
  def inputNaturalSequence (session: StatefulSession, accumulator: Accumulator, numbers: Numbers) : Unit = {
    try { // Inject candidates
      session.insert(Seq (accumulator): _*)
      session.insert (numbers.naturalSequence: _*)
      session.fire
      printResult(accumulator, numbers)
    } finally if (session != null) session.close()
  }

  test("PrimesFromNaturalSequence") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge = service.newKnowledge("PrimesFromNaturalSequence")
      .builder()
      .newRule("PrimesFromNaturalSequence")
      .forEach(
        "$accumulator", classOf[Accumulator],
        "$i1", classOf[Integer],
        "$i2", classOf[Integer],
        "$i3", classOf[Integer])
      .where("$i1 * $i2 == $i3")
      .execute((ctx: RhsContext) => {
        val accumulator = ctx.get[Accumulator]("$accumulator")
        val i1 = ctx.get[Int]("$i1")
        val i2 = ctx.get[Int]("$i2")
        val i3 = ctx.get[Int]("$i3")
        val newText: (Long,String) = (System.nanoTime(), s" Remove $i3 ->\t$i3 == $i1 * $i2")
        ctx.delete(i3)
        accumulator.compositeSet += i3
        accumulator.intervalTextSet += newText
      })
      .build()
    inputNaturalSequence(
      session = knowledge.newStatefulSession(),
      accumulator = Accumulator (),
      numbers = Numbers ()
    )
    service.shutdown()
  }

  test("PrimesFromNaturalSequence.rule") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge = service.newKnowledge("PrimesFromNaturalSequence.rule")
    PrimesFromNaturalSequence.rule (knowledge)
    inputNaturalSequence(
      session = knowledge.newStatefulSession(),
      accumulator = Accumulator (),
      numbers = Numbers(5)
    )
    service.shutdown()
  }

  test("AddAndRemoveRulesTest") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge: Knowledge = service.newKnowledge("AddAndRemoveRulesTest")
    AddNaturalSequence.rule (knowledge)
    RemoveCompositeNumbers.rule (knowledge)
    inputNaturalSequence(
      session = knowledge.newStatefulSession(),
      accumulator = Accumulator (),
      numbers = Numbers(10)
    )
    service.shutdown()
  }
}
