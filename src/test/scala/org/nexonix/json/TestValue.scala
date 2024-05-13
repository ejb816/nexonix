package org.nexonix.json

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite
import org.nexonix.json.Value.create

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
    val phoneNumber = create("phoneNumber", json, Array[String]("order", "customer", "contactDetails", "phone")).value
    println(phoneNumber)
  }

}
