package org.nexonix.rules.rete
import draco.PersistentTestLog

import draco.{ContentSink, DracoGenerator, SourceContent, TypeDefinition, TypeName}
import io.circe.{Json, parser}
import org.evrete.KnowledgeService
import org.nexonix.rules.rete.rules.TupleFact
import org.scalatest.funsuite.AnyFunSuite

class TupleFactReteTest extends AnyFunSuite with PersistentTestLog {
  val fact: (Int, Int, Int) = (1, 2, 3)
  test("TupleFactReteTest") {
    val resourcePath = "org/nexonix/rules/rete/TupleFact.json"
    val sourceContent = SourceContent(DracoGenerator.test.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    log.info(jsonContent.spaces2)

    val rule: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
    val ruleSource: String = DracoGenerator.generate(rule)
    val contentSink: ContentSink = ContentSink(DracoGenerator.test.sinkRoot, "org/nexonix/rules/rete/rules/TupleFact.scala")
    // Rule-ness is aspect presence, not a name suffix: TupleFact.json generates a bare object TupleFact
    contentSink.write(ruleSource)
    log.info(ruleSource)

    log.info(s"$fact")
  }

  test("TupleFact") {
    val service: KnowledgeService = new KnowledgeService()
    val knowledge = service.newKnowledge("TupleFact.rule")
    TupleFact.ruleType.pattern.accept(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.insert(Seq (fact): _*)
      session.fire()
    } finally if (session != null) session.close()
    service.shutdown()
  }
  test("ScalarFactRule") {}
}
