package org.nexonix.format.json

import draco.{Factory, Fixed, Generator, TypeElement, DomainName, Parameter, TypeDefinition, TypeName}
import io.circe.syntax.EncoderOps
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class TestTypeModule extends AnyFunSuite  {
  val dracoDomainName: DomainName = DomainName (
    TypeName ("Draco"),
    Seq (
      "Base"))
  println(s"${dracoDomainName.typeName.name} namePackage: ${dracoDomainName.typeName.namePackage}")

  val baseDomainName: DomainName = DomainName (
    TypeName ("Base", dracoDomainName.typeName.fullName),
    Seq (
      "Unit",
      "Orientable",
      "Coordinates"))
  println(s"${baseDomainName.typeName.name} namePackage: ${baseDomainName.typeName.namePackage}")

  val unitDomainName: DomainName  = DomainName (
    TypeName ("Unit", baseDomainName.typeName.fullName),
    Seq (
      "Measure",
      "Angle",
      "Radians",
      "Distance",
      "Meters"))
  println(s"${unitDomainName.typeName.name} namePackage: ${unitDomainName.typeName.namePackage}")

  val measureTypeName: TypeName = TypeName ("Measure", unitDomainName.typeName.fullName)
  val angleTypeName: TypeName = TypeName ("Angle", unitDomainName.typeName.fullName)
  val radiansTypeName: TypeName = TypeName ("Radians", unitDomainName.typeName.fullName)
  val distanceTypeName: TypeName = TypeName ("Distance", unitDomainName.typeName.fullName)
  val metersTypeName: TypeName = TypeName ("Meters", unitDomainName.typeName.fullName)

  val orientableDomainName: DomainName = DomainName (
    TypeName ("Orientable", baseDomainName.typeName.fullName),
    Seq (
      "Left",
      "Right",
      "Upper",
      "Lower",
      "Front",
      "Back"))
  println(s"${orientableDomainName.typeName.name} TypePackage: ${orientableDomainName.typeName.namePackage}")

  val valueParameter: Parameter = Parameter ("value", "T", "")
  val leftTypeName: TypeName = TypeName ("Left", orientableDomainName.typeName.fullName)
  val rightTypeName: TypeName = TypeName ("Right", orientableDomainName.typeName.fullName)
  val upperTypeName: TypeName = TypeName ("Upper", orientableDomainName.typeName.fullName)
  val lowerTypeName: TypeName = TypeName ("Lower", orientableDomainName.typeName.fullName)
  val frontTypeName: TypeName = TypeName ("Front", orientableDomainName.typeName.fullName)
  val backTypeName: TypeName = TypeName ("Back", orientableDomainName.typeName.fullName)
  val orientableParameters: Seq[Parameter] = Seq(valueParameter)
  val boundingBoxTypeName: TypeName = TypeName("BoundingBox", orientableDomainName.typeName.fullName)
  val leftElement: TypeElement = Fixed ("left", "Left[Radians]")
  val rightElement: TypeElement = Fixed ("right", "Right[Radians]")
  val upperElement: TypeElement = Fixed ("upper", "Upper[Radians]")
  val lowerElement: TypeElement = Fixed ("lower", "Lower[Radians]")
  val frontElement: TypeElement = Fixed ("front", "Front[Meters]")
  val backElement: TypeElement = Fixed ("back", "Back[Meters]")
  val boundingBoxElements: Seq[TypeElement] = Seq(
    leftElement,
    rightElement,
    upperElement,
    lowerElement,
    frontElement,
    backElement
  )
  val coordinatesDomainName: DomainName = DomainName (
    TypeName ("Coordinates", baseDomainName.typeName.fullName),
    Seq ("Spherical"))
  println(s"${coordinatesDomainName.typeName.name} TypePackage: ${coordinatesDomainName.typeName.namePackage}")

  val sphericalTypeName: TypeName = TypeName("Spherical", coordinatesDomainName.typeName.fullName)
  val azimuthParameter: Parameter = Parameter ("_azimuth", "Radians", "")
  val elevationParameter: Parameter = Parameter ("_elevation", "Radians", "")
  val rangeParameter: Parameter = Parameter ("_range", "Meters", "")
  val sphericalParameters: Seq[Parameter] = Seq (azimuthParameter, elevationParameter, rangeParameter)
  val azimuthElement: TypeElement = Fixed ("azimuth", "Radians", "_azimuth")
  val elevationElement: TypeElement = Fixed ("elevation", "Radians", "_elevation")
  val rangeElement: TypeElement = Fixed ("elevation", "Meters", "_range")
  val sphericalElements: Seq[TypeElement] = Seq (azimuthElement, elevationElement, rangeElement)
  val sphericalBoundsTypeName: TypeName = TypeName ("SphericalBounds", orientableDomainName.typeName.fullName)
  val lufParameter: Parameter = Parameter ("luf", "Spherical", "")
  val rlbParameter: Parameter = Parameter ("rlb", "Spherical", "")
  val leftBoundsElement: TypeElement = Fixed ("left", "Left[Radians]", "Left[Radians](_luf.azimuth)")
  val rightBoundsElement: TypeElement = Fixed ("right", "Right[Radians]", "Right[Radians](_rlb.azimuth)")
  val upperBoundsElement: TypeElement = Fixed ("upper", "Upper[Radians]", "Upper[Radians](_luf.elevation)")
  val lowerBoundsElement: TypeElement = Fixed ("lower", "Lower[Radians]", "Lower[Radians](_rlb.elevation)")
  val frontBoundsElement: TypeElement = Fixed ("front", "Front[Meters]", "Front[Meters](_luf.range)")
  val backBoundsElement: TypeElement = Fixed ("back", "Back[Meters]", "Back[Meters](_rlb.range)")
  val sphericalBoundsParameters: Seq[Parameter] = Seq (lufParameter, rlbParameter)
  val sphericalBoundsElements: Seq[TypeElement] = Seq (
    leftBoundsElement,
    rightBoundsElement,
    upperBoundsElement,
    lowerBoundsElement,
    frontBoundsElement,
    backBoundsElement
  )

  val tdList: Seq[TypeDefinition] = Seq[TypeDefinition] (
    TypeDefinition(
      _typeName = unitDomainName.typeName,
      _derivation = Seq (baseDomainName.typeName)
    ),
    TypeDefinition(
      _typeName = measureTypeName,
      _derivation = Seq (baseDomainName.typeName)
    ),
    TypeDefinition(
      _typeName = angleTypeName,
      _derivation = Seq (measureTypeName)
    ),
    TypeDefinition(
      _typeName = radiansTypeName,
      _derivation = Seq (angleTypeName)
    ),
    TypeDefinition(
      _typeName = distanceTypeName,
      _derivation = Seq (measureTypeName)
    ),
    TypeDefinition(
      _typeName = metersTypeName,
      _derivation = Seq (distanceTypeName)
    ),
    TypeDefinition(
      _typeName = leftTypeName,
      _derivation = Seq (measureTypeName),
      _factory = Factory(leftTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = rightTypeName,
      _derivation = Seq (measureTypeName),
      _factory = Factory(rightTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = upperTypeName,
      _derivation = Seq (measureTypeName),
      _factory = Factory(upperTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = lowerTypeName,
      _derivation = Seq (measureTypeName),
      _factory = Factory(lowerTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = frontTypeName,
      _derivation = Seq (measureTypeName),
      _factory = Factory(frontTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = backTypeName,
      _derivation = Seq (measureTypeName),
      _factory = Factory(backTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = boundingBoxTypeName,
      _derivation = Seq (orientableDomainName.typeName),
      _elements = boundingBoxElements,
      _factory = Factory(boundingBoxTypeName.fullName, orientableParameters)
    ),
    TypeDefinition(
      _typeName = sphericalTypeName,
      _derivation = Seq (coordinatesDomainName.typeName),
      _elements = sphericalElements,
      _factory = Factory(sphericalTypeName.fullName, sphericalParameters)
    ),
    TypeDefinition(
      _typeName = sphericalBoundsTypeName,
      _derivation = Seq (boundingBoxTypeName),
      _elements = sphericalBoundsElements,
      _factory = Factory(sphericalBoundsTypeName.fullName, sphericalBoundsParameters)
    )
  )
  def testTypeDefinitionEncode(td: TypeDefinition): Unit = {
    val sourceString: String = td.asJson.spaces2
    println(s"${td.typeName.fullName}:")
    println(sourceString)
  }

  def testTypeDefinitionDecode(td: TypeDefinition): Unit = {
    val sourceString: String = td.asJson.spaces2
    val jsonObject = parser.parse(sourceString).getOrElse(Json.Null)
    val tdInstance: TypeDefinition = jsonObject.as[TypeDefinition].toTry.get
    println(s"${tdInstance.typeName.fullName}:")
    println(tdInstance.asJson.spaces2)
  }
  def testTypeDefinitionGenerate(td: TypeDefinition): Unit = {
    println(Generator.generate(td))
  }

  test("Test Type Definition Encode") {
    tdList.map(testTypeDefinitionEncode)
  }

  test("Test Type Definition Decode") {
    tdList.map(testTypeDefinitionDecode)
  }

  test("Test Type Definition Generate") {
    tdList.map(testTypeDefinitionGenerate)
  }
}
