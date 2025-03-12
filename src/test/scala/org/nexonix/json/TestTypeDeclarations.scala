package org.nexonix.json
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.scalatest.funsuite.AnyFunSuite

sealed trait UserParams {
  val name: String
  val age: Int
  val country: String
}

object UserParams {
  def apply(
             _name : String = "Alice",
             _age : Int = 30,
             _country : String = "USA"
           ): UserParams = {
    new UserParams {
      val name: String = _name
      val age: Int = _age
      val country: String = _country
    }
  }

  // Manual Encoder
  implicit val encodeUserParams: Encoder[UserParams] = (u: UserParams) => Json.obj(
    "name" -> Json.fromString(u.name),
    "age" -> Json.fromInt(u.age),
    "country" -> Json.fromString(u.country)
  )

  // Manual Decoder
  implicit val decodeUserParams: Decoder[UserParams] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    age <- c.downField("age").as[Int]
    country <- c.downField("country").as[String]
  } yield UserParams(name, age, country)
}

class TestTypeDeclarations extends AnyFunSuite {
  test("Test UserParams") {
    val params = UserParams()
    val processed = s"Processing user: ${params.name}, ${params.age}, ${params.country}"
    println(processed)

    // Using the encoder via .asJson
    val json = params.asJson             // produces io.circe.Json
    println(s"Encoded JSON (noSpaces): ${json.noSpaces}")
    println(s"Encoded JSON (pretty):\n${json.spaces2}")
  }
}