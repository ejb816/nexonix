package draco.domain.primes

import draco.domain.{Rule, SourceContent, TypeDefinition, TypeName}
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class TestPrimesRules extends AnyFunSuite {
  test("Generate AddSequence") {
    val resourceClass = this.getClass
    val resourcePath = "/draco/domain/primes/AddSequence.json"
    val sourceContent = SourceContent(_resourcePath = resourcePath, _resourceClass = resourceClass)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: Rule = jsonContent.as[Rule].getOrElse(null)
    val ruleSource = TypeDefinition.generateRule(rule, Seq("draco", "domain", "primes"), Seq[TypeName]())
    println(ruleSource)
  }
  test("Generate RemoveFromSequence") {
    val resourceClass = this.getClass
    val resourcePath = "/draco/domain/primes/RemoveFromSequence.json"
    val sourceContent = SourceContent(_resourcePath = resourcePath, _resourceClass = resourceClass)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val rule: Rule = jsonContent.as[Rule].getOrElse(null)
    val ruleSource = TypeDefinition.generateRule(rule, Seq("draco", "domain", "primes"), Seq[TypeName]())
    println(ruleSource)
  }
}
