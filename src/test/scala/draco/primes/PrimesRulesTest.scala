package draco.primes

import draco.{ContentSink, Generator, PersistentTestLog, SourceContent, TypeDefinition, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.evrete.api.{Knowledge, RhsContext, StatefulSession}
import org.scalatest.funsuite.AnyFunSuite

class PrimesRulesTest extends AnyFunSuite with PersistentTestLog {
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
  test("Generate PrimesFromNaturalSequence") {
    val resourcePath = "draco/primes/PrimesFromNaturalSequence.json"
    val sourceContent = SourceContent(Generator.main.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    log.info(jsonContent.spaces2)

    val rule: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
    val ruleSource = Generator.generate (rule)
    val contentSink: ContentSink = ContentSink(Generator.main.sinkRoot, "draco/primes/PrimesFromNaturalSequenceRule.scala")
    contentSink.write(ruleSource)
    log.info(ruleSource)
  }

  test("Generate AddNaturalSequence") {
    val resourcePath = "draco/primes/AddNaturalSequence.json"
    val sourceContent = SourceContent(Generator.main.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    log.info(jsonContent.spaces2)

    val rule: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
    val ruleSource = Generator.generate (rule)
    val contentSink: ContentSink = ContentSink(Generator.main.sinkRoot, "draco/primes/AddNaturalSequenceRule.scala")
    contentSink.write(ruleSource)
    log.info(ruleSource)
  }

  test("Generate RemoveCompositeNumbers") {
    val resourcePath = "draco/primes/RemoveCompositeNumbers.json"
    val sourceContent = SourceContent(Generator.main.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    log.info(jsonContent.spaces2)

    val rule: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
    val ruleSource = Generator.generate (rule)
    val contentSink: ContentSink = ContentSink(Generator.main.sinkRoot, "draco/primes/RemoveCompositeNumbersRule.scala")
    contentSink.write(ruleSource)
    log.info(ruleSource)
  }

  def printResult (accumulator: Accumulator, numbers: Numbers) : Unit = {
    def indexDifference (list: List[Int]): List[Int] = if (list.length < 2) List.empty else list.zip(list.tail).map { case (a, b) => b - a }

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
    log.info(s"Input Natural.json.json Sequence:${numbers.naturalSequence.toList.toString().substring(prefixLength)}")
    log.info(s"Result Prime Sequence: ${primeList.sorted.toString().substring(prefixLength)}")
    val firstOrder = indexDifference(primeList.sorted)
    val twinPrimeIndices = firstOrder.zipWithIndex.collect { case (2, i) => i }
    val secondOrder = indexDifference(twinPrimeIndices)
    log.info(s"Result Inter-prime Difference Sequence: ${firstOrder.toString().substring(prefixLength)}")
    log.info(s"Result Twin Prime Index Sequence: ${twinPrimeIndices.toString().substring(prefixLength)}")
    log.info(s"Result Twin Prime Spacing Sequence: ${secondOrder.toString().substring(prefixLength)}")
    log.info(s"Result Composite Sequence: ${accumulator.compositeSet.toList.sorted.toString().substring(prefixLength)}")
    log.info(s"firstTime = $firstTime")
    log.info(s"Rule Results with Fire Interval:\n00000${timedText.mkString("\n")}")
  }
  def inputNaturalSequence (session: StatefulSession, accumulator: Accumulator, numbers: Numbers) : Unit = {
    try { // Inject candidates
      session.insert(Seq (accumulator): _*)
      session.insert (numbers.naturalSequence: _*)
      session.fire
      printResult(accumulator, numbers)
    } finally if (session != null) session.close()
  }

  test("PrimesFromNaturalSequence.rule") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge = service.newKnowledge("PrimesFromNaturalSequence.rule")
    PrimesFromNaturalSequenceRule.ruleType.pattern.accept(knowledge)
    inputNaturalSequence(
      session = knowledge.newStatefulSession(),
      accumulator = Accumulator (),
      numbers = Numbers(10)
    )
    service.shutdown()
  }

  test("AddAndRemoveRulesTest") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge: Knowledge = service.newKnowledge("AddAndRemoveRulesTest")
    AddNaturalSequenceRule.ruleType.pattern.accept(knowledge)
    RemoveCompositeNumbersRule.ruleType.pattern.accept(knowledge)
    inputNaturalSequence(
      session = knowledge.newStatefulSession(),
      accumulator = Accumulator (),
      numbers = Numbers(10)
    )
    service.shutdown()
  }
}
