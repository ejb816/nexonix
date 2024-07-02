package org.nexonix.json

import org.scalatest.funsuite.AnyFunSuite
import io.circe._
import io.circe.parser._
import io.circe.optics.JsonPath._
import monocle.Optional

class TestCirceJson extends AnyFunSuite {

  test("Circe Examples") {
    val json: Json = parse("""
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

    val phoneNumFromCursor: Option[String] = json.hcursor.
      downField("order").
      downField("customer").
      downField("contactDetails").
      get[String]("phone").
      toOption
    // phoneNumFromCursor: Option[String] = Some(value = "0123-456-789")
    println(phoneNumFromCursor)

    val _phoneNum: Optional[Json, String] = root.order.customer.contactDetails.phone.string
    // _phoneNum: monocle.package.Optional[Json, String] = monocle.POptional$$anon$1@27d9dc98

    val phoneNum: Option[String] = _phoneNum.getOption(json)
    // phoneNum: Option[String] = Some(value = "0123-456-789")
    println(phoneNum)

    val itemsFromCursor: Vector[Json] = json.hcursor.
      downField("order").
      downField("items").
      focus.
      flatMap(_.asArray).
      getOrElse(Vector.empty)
    // itemsFromCursor: Vector[Json] = Vector(
    //   JObject(value = object[id -> 123,description -> "banana",quantity -> 1]),
    //   JObject(value = object[id -> 456,description -> "apple",quantity -> 2])
    // )

    val quantities: Vector[Int] =
      itemsFromCursor.flatMap(_.hcursor.get[Int]("quantity").toOption)
    // quantities: Vector[Int] = Vector(1, 2)
    println(quantities)

    val items: List[Int] =
      root.order.items.each.quantity.int.getAll(json)
    // items: List[Int] = List(1, 2)
    println(items)
  }

  test("Extended Examples") {
  }

}
