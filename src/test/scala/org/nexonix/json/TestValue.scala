package org.nexonix.json

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite
import org.nexonix.json.Value.define

class TestValue extends AnyFunSuite {
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
  test("Test Value") {
    val phoneNumber = define("phoneNumber", json, Array[String]("order", "customer", "contactDetails", "phone")).value[String]
    println(phoneNumber)
    val itemID = define("itemID", json, Array[String]("order", "items", "0", "id")).value[Integer]
    println(itemID)
  }

}
