package draco

import io.circe.{Json, parser}
import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite
class ValueTest extends AnyFunSuite {
  val sphericalJsonString: String =
    """{
      |  "typeName": {
      |    "name": "Spherical",
      |    "namePackage": [
      |      "draco",
      |      "base",
      |      "coordinate"
      |      ],
      |    "parent": "draco.base.coordinate.Coordinate"
      |  },
      |  "typeParameters": [],
      |  "moduleElements": [],
      |  "dependsOn": [
      |    {
      |      "name": "Radians",
      |      "namePackage": [
      |        "draco",
      |        "base",
      |        "unit"
      |      ],
      |      "parent": "draco.base.unit.Unit"
      |    },
      |    {
      |      "name": "Meters",
      |      "namePackage": [
      |        "draco",
      |        "base",
      |        "unit"
      |      ],
      |      "parent": "draco.base.unit.Unit"
      |    }
      |  ],
      |  "derivesFrom": [
      |    {
      |      "name": "Coordinate",
      |      "namePackage": [
      |        "draco",
      |        "base",
      |        "coordinate"
      |      ],
      |      "parent": "draco.base.Base"
      |    }
      |  ],
      |  "elements": [
      |    {"kind": "Fixed", "name": "azimuth", "type": "Radians", "value": ""},
      |    {"kind": "Fixed", "name": "elevation", "type": "Radians", "value": ""},
      |    {"kind": "Fixed", "name": "range", "type": "Meters", "value": ""}
      |  ],
      |  "parameters": [
      |    {"kind": "Parameter", "name": "azimuth", "type": "Radians", "value": ""},
      |    {"kind": "Parameter", "name": "elevation", "type": "Radians", "value": ""},
      |    {"kind": "Parameter", "name": "range", "type": "Meters", "value": ""}
      |  ],
      |  "rules": []
      |}""".stripMargin

  test ("Value with notional TypeDefinition content for Spherical Coordinate type") {
    val jsonTypeDefinition: Json = parser.parse(sphericalJsonString).getOrElse(Json.Null)
    val spherical: TypeDefinition = Value ("spherical", jsonTypeDefinition, Seq ()).value[TypeDefinition]
    println(s"Test1:\n${spherical.asJson.spaces2}")
    val typeName: TypeName = Value ("typeName", jsonTypeDefinition, Seq ("typeName")).value[TypeName]
    val dependsOn0: TypeName = Value ("dependsOn0", jsonTypeDefinition, Seq ("dependsOn", "0")).value[TypeName]
    val dependsOn1: TypeName = Value ("dependsOn1", jsonTypeDefinition, Seq ("dependsOn", "1")).value[TypeName]
    val derivesFrom0: TypeName = Value ("derivesFrom0", jsonTypeDefinition, Seq ("derivesFrom", "0")).value[TypeName]
    val elements0: TypeElement = Value ("elements0", jsonTypeDefinition, Seq ("elements", "0")).value[TypeElement]
    val elements1: TypeElement = Value ("elements1", jsonTypeDefinition, Seq ("elements", "1")).value[TypeElement]
    val elements2: TypeElement = Value ("elements2", jsonTypeDefinition, Seq ("elements", "2")).value[TypeElement]
    val parameters0: TypeElement = Value ("parameters0", jsonTypeDefinition, Seq ("parameters", "0")).value[TypeElement]
    val parameters1: TypeElement = Value ("parameters1", jsonTypeDefinition, Seq ("parameters", "1")).value[TypeElement]
    val parameters2: TypeElement = Value ("parameters2", jsonTypeDefinition, Seq ("parameters", "2")).value[TypeElement]
    val result: String =
      s"""{
         |  "typeName": ${typeName.asJson.spaces2},
         |  "typeParameters": [],
         |  "moduleElements": [],
         |  "dependsOn": [
         |    ${dependsOn0.asJson.spaces2},
         |    ${dependsOn1.asJson.spaces2}
         |  ],
         |  "derivesFrom": [
         |    ${derivesFrom0.asJson.spaces2}
         |  ],
         |  "elements": [
         |    ${elements0.asJson.spaces2},
         |    ${elements1.asJson.spaces2},
         |    ${elements2.asJson.spaces2}
         |  ],
         |  "parameters": [
         |    ${parameters0.asJson.spaces2},
         |    ${parameters1.asJson.spaces2},
         |    ${parameters2.asJson.spaces2}
         |  ],
         |  "rules": []
         |}""".stripMargin
    val jsonTypeDefinitionCheck: Json = parser.parse(result).getOrElse(Json.Null)
    println(s"Test2:\n${jsonTypeDefinitionCheck.spaces2}")
    assert(spherical.asJson.spaces2 == jsonTypeDefinitionCheck.spaces2)
  }
}
