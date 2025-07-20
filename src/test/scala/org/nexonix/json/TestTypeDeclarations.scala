package org.nexonix.json

import draco.{Generator, Parameter, TypeDefinition, TypeDictionary, TypeName}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}
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

  val td: TypeDefinition = draco.TypeDefinition (
    _typeName = TypeName (_name = "Left", _packageName = "Orientable", _namePackage = Seq("draco", "domain")),
    _typeParameters = Seq("T"),
    _dependsOn = Seq(),
    _derivesFrom = Seq( TypeName(_name = "Measure", _packageName = "Unit", _namePackage = Seq("draco", "domain"))),
    _members = Seq(Parameter(_name = "value", _type = "T", _value = "")),
    _parameters = Seq(),
    _rules = Seq()
  )
  test("Type Definition") {
    val tdJson = td.asJson
    println(tdJson.spaces2)
    println(Generator.generate(td))
  }



  test("Test DomainDictionary") {
    val alpha: TypeDefinition = draco.TypeDefinition (
      _typeName = TypeName (_name = "Alpha", _packageName = "Alpha", _namePackage = Seq("draco", "domain")),
      _derivesFrom = Seq (TypeName (_name = "DataModel", _packageName = "DataModel", _namePackage = Seq("draco", "domain"))))
    val bravo: TypeDefinition = draco.TypeDefinition (
      _typeName = TypeName (_name = "Bravo", _packageName = "Bravo", _namePackage = Seq ("draco", "domain")),
      _derivesFrom = Seq (TypeName (_name = "DataModel", _packageName = "DataModel",  _namePackage = Seq ("draco", "domain"))))
    val charlie: TypeDefinition = draco.TypeDefinition (
      _typeName = TypeName (_name = "Charlie", _packageName = "Charlie", _namePackage = Seq ("draco", "domain")),
      _derivesFrom = Seq (TypeName (_name = "DataModel", _packageName = "DataModel",  _namePackage = Seq ("draco", "domain"))))
    val delta: TypeDefinition = draco.TypeDefinition (
      _typeName = TypeName(_name = "Delta", _packageName = "Delta", _namePackage = Seq ("draco", "domain")),
      _derivesFrom = Seq (TypeName (_name = "DataModel", _packageName = "DataModel",  _namePackage = Seq ("draco", "domain"))))
    val dataModel: TypeDefinition = draco.TypeDefinition (
      _typeName = TypeName (_name = "DataModel", _packageName = "DataModel", _namePackage = Seq ("draco", "domain"))
    )
    val map = Map[TypeName,TypeDefinition](
      (dataModel.typeName,  dataModel),
      (alpha.typeName,  alpha),
      (bravo.typeName,  bravo),
      (charlie.typeName,  charlie),
      (delta.typeName,  delta))

    val alphaDictionary = TypeDictionary (typeName = TypeName("Alpha", "Alpha", Seq("draco", "domain")))
    val processed = s"Processing type definition dictionary: ${alphaDictionary.asJson.spaces2}"
    println(processed)

    // Using the encoder via .asJson
    println(alphaDictionary.kvMap.toSeq)
  }

  test("Test TypeDefinitionDomainDictionary") {

  }
  test("Test TransformDomain") {

  }
  test("Test TransformDomainDictionary") {

  }
}