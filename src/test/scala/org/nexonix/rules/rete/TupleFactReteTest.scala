package org.nexonix.rules.rete
import draco.PersistentTestLog

import draco.{ContentSink, Generator, SourceContent, TypeDefinition, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.nexonix.rules.rete.rules.TupleFactRule
import org.scalatest.funsuite.AnyFunSuite

class TupleFactReteTest extends AnyFunSuite with PersistentTestLog {
  val fact: (Int, Int, Int) = (1, 2, 3)
  test("TupleFactReteTest") {
    val resourcePath = "org/nexonix/rules/rete/TupleFact.rule.json"
    val sourceContent = SourceContent(Generator.test.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    log.info(jsonContent.spaces2)

    val rule: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
    val ruleSource: String = Generator.generate(rule)
    val contentSink: ContentSink = ContentSink(Generator.test.sinkRoot, "org/nexonix/rules/rete/rules/TupleFactRule.scala")
    // Note: Generator now auto-appends "Rule" suffix, so TupleFact.rule.json generates TupleFactRule
    contentSink.write(ruleSource)
    log.info(ruleSource)

    log.info(s"$fact")
  }

  test("TupleFactRule") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge = service.newKnowledge("TupleFactRule.rule")
    TupleFactRule.ruleType.pattern.accept(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.insert(Seq (fact): _*)
      session.fire()
    } finally if (session != null) session.close()
    service.shutdown()
  }
  test("ScalarFactRule") {}
}
