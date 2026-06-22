package org.nexonix.format.json
import draco.PersistentTestLog

import draco.{Generator, SourceContent, TypeDefinition, TypeName, Value}
import io.circe.syntax.EncoderOps
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class TestValue extends AnyFunSuite with PersistentTestLog {
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
    log.info(phoneNumber)
    val itemID = Value("itemID", Seq[String]("order", "items", "0", "id")).value[Int](json)
    log.info(s"$itemID")
  }
  test ("test rule json") {
    val jsonFilePaths: Seq[String] = Seq (
      "draco/primes/AddNaturalSequence.rule.json",
      "draco/primes/PrimesFromNaturalSequence.rule.json",
      "draco/primes/RemoveCompositeNumbers.rule.json"
    )
    val checkJson: String => Unit = fn => {
      val sourceContent = SourceContent(Generator.main.sourceRoot, fn)
      val content = sourceContent.sourceLines.mkString("\n")
      val jsonContent: Json = parser.parse(content).getOrElse(Json.Null)
      val rule = jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
      val ruleSource: String = Generator.generate(rule)
      if (jsonContent.equals(rule.asJson)) {
        log.info(content)
      } else {
        val checked =
          s"""Content of $fn does not exactly match encoder result:
             |Check emitted rule source code against current source code.
             |""".stripMargin
        log.info(checked)
      }
      log.info(ruleSource)
    }
    jsonFilePaths.map (checkJson)
  }
}
