package org.nexonix.rules.rete

import draco.{ContentSink, Generator, RuleDefinition, SourceContent, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.nexonix.rules.rete.rules.TupleFactRule
import org.scalatest.funsuite.AnyFunSuite

class TupleFactReteTest extends AnyFunSuite {
  val fact: (Int, Int, Int) = (1, 2, 3)
  test("TupleFactReteTest") {
    val resourcePath = "org/nexonix/rules/rete/rules/TupleFactRule.json"
    val sourceContent = SourceContent(Generator.test.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: RuleDefinition = jsonContent.as[RuleDefinition].getOrElse(null)
    val ruleSource: String = Generator.generate(rule)
    val contentSink: ContentSink = ContentSink(Generator.test.sinkRoot, "org/nexonix/rules/rete/rules/TupleFactRule.scala")
    contentSink.write(ruleSource)
    println(ruleSource)

    println(fact)
  }

  test("TupleFactRule") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge = service.newKnowledge("TupleFactRule.rule")
    TupleFactRule.pattern (knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.insert(Seq (fact): _*)
      session.fire()
    } finally if (session != null) session.close()
    service.shutdown()
  }
  test("ScalarFactRule") {}
}
