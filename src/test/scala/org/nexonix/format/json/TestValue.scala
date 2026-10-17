package org.nexonix.format.json
import draco.PersistentTestLog

import draco.{DracoGenerator, SourceContent, TypeDefinition, TypeName}
import draco.format.dracon.{Dracon, Value}
import draco.scalatarget.CirceCodecs
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
    val phoneNumber = Value("phoneNumber", Seq[String]("order", "customer", "contactDetails", "phone")).decode[String](Dracon(json), CirceCodecs.decoder(io.circe.Decoder.decodeString))
    assert(phoneNumber.fold(false, _ == "0123-456-789"))
    log.info(phoneNumber.fold("<absent>", identity))
    val itemID = Value("itemID", Seq[String]("order", "items", "0", "id")).decode[Int](Dracon(json), CirceCodecs.decoder(io.circe.Decoder.decodeInt))
    assert(itemID.fold(false, _ == 123))
    log.info(s"$itemID")
  }
  test ("test rule json") {
    val jsonFilePaths: Seq[String] = Seq (
      "draco/primes/AddNaturalSequence.json",
      "draco/primes/PrimesFromNaturalSequence.json",
      "draco/primes/RemoveCompositeNumbers.json"
    )
    val checkJson: String => Unit = fn => {
      val sourceContent = SourceContent(DracoGenerator.main.sourceRoot, fn)
      val content = sourceContent.sourceLines.mkString("\n")
      val jsonContent: Json = parser.parse(content).getOrElse(Json.Null)
      val rule = jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
      val ruleSource: String = DracoGenerator.generate(rule)
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
