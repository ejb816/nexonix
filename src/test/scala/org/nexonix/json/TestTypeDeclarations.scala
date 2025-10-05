package org.nexonix.json

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}
import org.nexonix.actor.DataModelService
import org.scalatest.funsuite.AnyFunSuite


class TestTypeDeclarations extends AnyFunSuite {

  trait UserParameters {
    val name: String
    val age: Int
    val country: String
  }

  object UserParameters {
    def apply (
                _name: String = "Alice",
                _age: Int = 30,
                _country: String = "USA"
              ) : UserParameters = new UserParameters {
      val name: String = _name
      val age: Int = _age
      val country: String = _country
    }
    lazy implicit val encoder: Encoder[UserParameters] = Encoder.instance { up =>
      Json.obj(
        "name" -> up.name.asJson,
        "age" -> up.age.asJson,
        "country"-> up.country.asJson
      )
    }
    lazy implicit val decoder: Decoder[UserParameters] = Decoder.instance { cursor =>
      for {
          _name <- cursor.downField("name").as[String]
          _age <- cursor.downField("age").as[Int]
          _country <- cursor.downField("country").as[String]
      } yield UserParameters (
        _name,
        _age,
        _country
      )
    }
  }


  test("Test UserParameters") {
    val jsonString = s"""{
                        |  "name" : "Alice",
                        |  "age" : 30,
                        |  "country" : "USA"
                        |}""".stripMargin
    val jsonUserParameters = io.circe.parser.parse(jsonString).getOrElse(Json.Null)
    val userParameters = jsonUserParameters.as[UserParameters].toTry.get

    // Using the encoder via .asJson
    val json = userParameters.asJson             // produces io.circe.Json
    println(s"Encoded JSON:\n${json.spaces2}")
    val processed =
      s"""Processing decoded UserParameters:
         |  ${userParameters.name},
         |  ${userParameters.age},
         |  ${userParameters.country}""".stripMargin
    println(processed)
  }

  test("Test DataModelService") {
    val dataModelService: DataModelService = DataModelService()
    println(dataModelService.domainPackage.typeDefinition.typeName.fullName)
    println(dataModelService.domainPackage.subDomainNames.mkString(","))
    println(dataModelService.dataModel.typeDefinition.typeName.fullName)
    println(dataModelService.dataModel.subDomainNames.mkString(","))
  }

  test("Test TypeDefinitionDomainDictionary") {

  }
  test("Test TransformDomain") {

  }
  test("Test TransformDomainDictionary") {

  }
}