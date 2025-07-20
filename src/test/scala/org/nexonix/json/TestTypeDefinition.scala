package org.nexonix.json

import draco.TypeDefinition
import io.circe.syntax.EncoderOps
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class TestTypeDefinition extends AnyFunSuite  {
  test("Test Abstract Type Definition domain.unit.Measure") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Measure", "domain": "Unit", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Base", "domain": "", "namePackage": [ "draco", "domain" ] }],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

   println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Abstract Type Definition domain.unit.Angle") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Angle", "domain": "Unit", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "Unit",  "namePackage": [ "draco", "domain" ] }],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Abstract Type Definition domain.unit.Radians") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Radians", "domain": "", "namePackage": [ "draco", "domain", "unit" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Angle", "domain": "", "namePackage": [ "draco", "domain", "unit" ] }],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Abstract Type Definition domain.unit.Distance") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Distance", "domain": "Unit", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "Unit", "namePackage": [ "draco", "domain" ] }],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Abstract Type Definition domain.unit.Meters") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Meters", "domain": "Unit", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Distance", "domain": "Unit", "namePackage": [ "draco", "domain"] }],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Generic Type Definition domain.orientable.Left") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Left", "domain": "Orientable", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "Unit", "namePackage": [ "draco", "domain" ] }],
        |  "typeParameters": ["T"],
        |  "parameters": [ { "kind": "Parameter", "name": "value", "type": "T", "value": "" } ],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Generic Type Definition domain.orientable.Right") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Right", "domain": "Orientable", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "Unit", "namePackage": [ "draco", "domain" ] }],
        |  "typeParameters": ["T"],
        |  "parameters": [ { "kind": "Parameter", "name": "value", "type": "T", "value": "" } ],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Generic Type Definition domain.orientable.Upper") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Upper", "domain": "", "namePackage": [ "draco", "domain", "orientable" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "", "namePackage": [ "draco", "domain", "unit" ] }],
        |  "typeParameters": ["T"],
        |  "parameters": [ { "kind": "Parameter", "name": "value", "type": "T", "value": "" } ],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get
    println(jsonAbstract.spaces2)
    println(tdAbstract.asJson.spaces2)
    // println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Generic Type Definition domain.orientable.Lower") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Lower", "domain": "", "namePackage": [ "draco", "domain", "orientable" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "", "namePackage": [ "draco", "domain", "unit" ] } ],
        |  "typeParameters": ["T"],
        |  "parameters": [ { "kind": "Parameter", "name": "value", "type": "T", "value": "" } ],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get
    println(jsonAbstract.spaces2)
    println(tdAbstract.asJson.spaces2)
    // println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Generic Type Definition domain.orientable.Front") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Front", "domain": "", "namePackage": [ "draco", "domain", "orientable" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "", "namePackage": [ "draco", "domain", "unit" ] } ],
        |  "typeParameters": ["T"],
        |  "parameters": [ { "kind": "Parameter", "name": "value", "type": "T", "value": "" } ],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get
    println(jsonAbstract.spaces2)
    println(tdAbstract.asJson.spaces2)
    // println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Generic Type Definition domain.orientable.Back") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Back", "domain": "", "namePackage": [ "draco", "domain", "orientable" ] },
        |  "dependsOn": [],
        |  "derivesFrom": [ { "name": "Measure", "domain": "", "namePackage": [ "draco", "domain", "unit" ] } ],
        |  "typeParameters": ["T"],
        |  "parameters": [ { "kind": "Parameter", "name": "value", "type": "T", "value": "" } ],
        |  "members": [],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get
    println(jsonAbstract.spaces2)
    println(tdAbstract.asJson.spaces2)
    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Abstract Type Definition BoundingBox") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "BoundingBox", "domain": "", "namePackage": [ "draco", "domain", "orientable" ] },
        |  "dependsOn": [
        |  { "name": "Meters", "domain": "", "namePackage": [ "draco", "domain", "unit" ] },
        |  { "name": "Radians", "domain": "", "namePackage": [ "draco", "domain", "unit" ] } ],
        |  "derivesFrom": [ { "name": "Base", "domain": "", "namePackage": [ "draco", "domain" ] } ],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [
        |  { "kind": "Fixed", "name": "left", "type": "Left[Radians]", "value": ""},
        |  { "kind": "Fixed", "name": "right", "type": "Right[Radians]", "value": ""},
        |  { "kind": "Fixed", "name": "upper", "type": "Upper[Radians]", "value": ""},
        |  { "kind": "Fixed", "name": "lower", "type": "Lower[Radians]", "value": ""},
        |  { "kind": "Fixed", "name": "front", "type": "Front[Meters]", "value": ""},
        |  { "kind": "Fixed", "name": "back", "type": "Back[Meters]", "value": ""} ],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get
    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Abstract Type Definition Spherical") {
    val sourceStringAbstract: String =
      """{
        |  "typeName": { "name": "Spherical", "domain": "Coordinates", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [
        |  { "name": "Meters", "domain": "Unit", "namePackage": [ "draco", "domain" ] },
        |  { "name": "Radians", "domain": "Unit", "namePackage": [ "draco", "domain" ] }],
        |  "derivesFrom": [ { "name": "Base", "domain": "", "namePackage": [ "draco", "domain" ] } ],
        |  "typeParameters": [],
        |  "parameters": [],
        |  "members": [
        |  { "kind": "Fixed", "name": "azimuth", "type": "Radians", "value": "" },
        |  { "kind": "Fixed", "name": "elevation", "type": "Radians", "value": "" },
        |  { "kind": "Fixed", "name": "range", "type": "Meters", "value": "" } ],
        |  "rules": []
        |}""".stripMargin
    val jsonAbstract = parser.parse(sourceStringAbstract).getOrElse(Json.Null)
    val tdAbstract: TypeDefinition = jsonAbstract.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdAbstract))
  }
  test("Test Concrete Type Definition BoundingBoxFromSpherical") {
    val sourceStringConcrete: String =
      """{
        |  "typeName": { "name": "BoundingBoxFromSpherical", "domain": "Orientable", "namePackage": [ "draco", "domain" ] },
        |  "dependsOn": [
        |  { "name": "Spherical", "domain": "Coordinates", "namePackage": [ "draco", "domain" ] },
        |  { "name": "Meters", "domain": "Unit", "namePackage": [ "draco", "domain" ] },
        |  { "name": "Radians", "domain": "Unit", "namePackage": [ "draco", "domain" ] } ],
        |  "derivesFrom": [ { "name": "BoundingBox", "domain": "Orientable", "namePackage": [ "draco", "domain" ] } ],
        |  "typeParameters": [],
        |  "parameters": [
        |  { "kind": "Parameter", "name": "luf", "type": "Spherical", "value": ""},
        |  { "kind": "Parameter", "name": "rlb", "type": "Spherical", "value": ""} ],
        |  "members": [
        |  { "kind": "Fixed", "name": "left", "type": "Left[Radians]", "value": "Left[Radians](_luf.azimuth)"},
        |  { "kind": "Fixed", "name": "right", "type": "Right[Radians]", "value": "Right[Radians](_rlb.azimuth)"},
        |  { "kind": "Fixed", "name": "upper", "type": "Upper[Radians]", "value": "Upper[Radians](_luf.elevation)"},
        |  { "kind": "Fixed", "name": "lower", "type": "Lower[Radians]", "value": "Lower[Radians](_rlb.elevation)"},
        |  { "kind": "Fixed", "name": "front", "type": "Front[Radians]", "value": "Front[Meters](_luf.range)"},
        |  { "kind": "Fixed", "name": "back", "type": "Back[Radians]", "value": "Back[Meters](_rlb.range)"} ],
        |  "rules": []
        |}""".stripMargin
    val jsonConcrete = parser.parse(sourceStringConcrete).getOrElse(Json.Null)
    val tdConcrete: TypeDefinition = jsonConcrete.as[TypeDefinition].toTry.get

    println(TypeDefinition.generate(tdConcrete))
  }
}
