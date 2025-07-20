package org.nexonix.json

import draco.{SourceContent, TypeDefinition, Value}
import draco.domain.TypeName
import draco.rule.Rule
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class TestValue extends AnyFunSuite {
  test("Test Value") {
    val json: Json = parser.parse("""
{
  "order": {
    "customer": {
      "name": "Custy McCustomer",
      "contactDetails": {
        "address": "1 Fake Street, London, England",
        "phone": "0123-456-789"
      }
    },
    "items": [{
      "id": 123,
      "description": "banana",
      "quantity": 1
    }, {
      "id": 456,
      "description": "apple",
      "quantity": 2
    }],
    "total": 123.45
  }
}
""").getOrElse(Json.Null)
    val phoneNumber = Value("phoneNumber", json, Seq[String]("order", "customer", "contactDetails", "phone")).value[String]
    println(phoneNumber)
    val itemID = Value("itemID", json, Seq[String]("order", "items", "0", "id")).value[Int]
    println(itemID)
  }
  test ("test rule json") {
    val defaultRule = Rule()
    val sourceContent = SourceContent(_resourcePath = "/draco/domain/primes/AddSequence.json", _resourceClass = classOf[TestValue])
    val content = sourceContent.sourceLines.mkString("\n")
    println(content)
    val jsonContent: Json = parser.parse(content).getOrElse(Json.Null)
    val rule = jsonContent.as[Rule].getOrElse(defaultRule)
    val ruleSource: String = TypeDefinition.generateRule(rule, Seq("draco","domain","primes"), Seq[TypeName]())
    println(ruleSource)
  }
}
