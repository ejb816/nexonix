package org.nexonix.format.json

import draco.{Generator, RuleDefinition, SourceContent, TypeDefinition, TypeName, Value}
import io.circe.syntax.EncoderOps
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
    val phoneNumber = Value("phoneNumber", Seq[String]("order", "customer", "contactDetails", "phone")).value[String](json)
    println(phoneNumber)
    val itemID = Value("itemID", Seq[String]("order", "items", "0", "id")).value[Int](json)
    println(itemID)
  }
  test ("test rule json") {
    val jsonFilePaths: Seq[String] = Seq (
      "draco/primes/rules/AddNaturalSequence.json",
      "draco/primes/rules/PrimesFromNaturalSequence.json",
      "draco/primes/rules/RemoveCompositeNumbers.json"
    )
    val checkJson: String => Unit = fn => {
      val sourceContent = SourceContent(Generator.main.sourceRoot, fn)
      val content = sourceContent.sourceLines.mkString("\n")
      val jsonContent: Json = parser.parse(content).getOrElse(Json.Null)
      val rule = jsonContent.as[RuleDefinition].getOrElse(RuleDefinition.Null)
      val ruleSource: String = Generator.generate(rule)
      if (jsonContent.equals(rule.asJson)) {
        println(content)
      } else {
        val checked =
          s"""Content of $fn does not exactly match encoder result:
             |Check emitted rule source code against current source code.
             |""".stripMargin
        print(checked)
      }
      println(ruleSource)
    }
    jsonFilePaths.map (checkJson)
  }
}
