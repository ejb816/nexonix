package org.nexonix.format.json
import draco.PersistentTestLog

import draco.{DomainAspect, DracoAspect, Factory, Fixed, Generator, Parameter, TypeDefinition, TypeElement, TypeName}
import io.circe.syntax.EncoderOps
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class TestTypeModule extends AnyFunSuite with PersistentTestLog  {
  val dracoDomainDefinition: TypeDefinition = TypeDefinition (
    TypeName ("Draco", _namePackage = Seq("draco")),
    _domainAspect = DomainAspect (_elementTypeNames = Seq (
      "Base")))
  log.info(s"${dracoDomainDefinition.typeName.name} namePackage: ${dracoDomainDefinition.typeName.namePackage}")

  val baseDomainDefinition: TypeDefinition = TypeDefinition (
    TypeName ("Base", _namePackage = Seq("draco", "base")),
    _domainAspect = DomainAspect (_elementTypeNames = Seq (
      "Unit",
      "Orientable",
      "Coordinates")))
  log.info(s"${baseDomainDefinition.typeName.name} namePackage: ${baseDomainDefinition.typeName.namePackage}")

  val unitDomainDefinition: TypeDefinition = TypeDefinition (
    TypeName ("Unit", _namePackage = Seq("draco", "base", "unit")),
    _domainAspect = DomainAspect (_elementTypeNames = Seq (
      "Measure",
      "Angle",
      "Radians",
      "Distance",
      "Meters")))
  log.info(s"${unitDomainDefinition.typeName.name} namePackage: ${unitDomainDefinition.typeName.namePackage}")

  val measureTypeName: TypeName = TypeName ("Measure", _namePackage = Seq("draco", "base", "unit"))
  val angleTypeName: TypeName = TypeName ("Angle", _namePackage = Seq("draco", "base", "unit"))
  val radiansTypeName: TypeName = TypeName ("Radians", _namePackage = Seq("draco", "base", "unit"))
  val distanceTypeName: TypeName = TypeName ("Distance", _namePackage = Seq("draco", "base", "unit"))
  val metersTypeName: TypeName = TypeName ("Meters", _namePackage = Seq("draco", "base", "unit"))

  val orientableDomainDefinition: TypeDefinition = TypeDefinition (
    TypeName ("Orientable", _namePackage = Seq("draco", "base", "orientable")),
    _domainAspect = DomainAspect (_elementTypeNames = Seq (
      "Left",
      "Right",
      "Upper",
      "Lower",
      "Front",
      "Back")))
  log.info(s"${orientableDomainDefinition.typeName.name} TypePackage: ${orientableDomainDefinition.typeName.namePackage}")

  val valueParameter: Parameter = Parameter ("value", "T", Json.Null)
  val leftTypeName: TypeName = TypeName ("Left", _namePackage = Seq("draco", "base", "orientable"))
  val rightTypeName: TypeName = TypeName ("Right", _namePackage = Seq("draco", "base", "orientable"))
  val upperTypeName: TypeName = TypeName ("Upper", _namePackage = Seq("draco", "base", "orientable"))
  val lowerTypeName: TypeName = TypeName ("Lower", _namePackage = Seq("draco", "base", "orientable"))
  val frontTypeName: TypeName = TypeName ("Front", _namePackage = Seq("draco", "base", "orientable"))
  val backTypeName: TypeName = TypeName ("Back", _namePackage = Seq("draco", "base", "orientable"))
  val orientableParameters: Seq[Parameter] = Seq(valueParameter)
  val boundingBoxTypeName: TypeName = TypeName("BoundingBox", _namePackage = Seq("draco", "base", "orientable"))
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
  val coordinatesDomainDefinition: TypeDefinition = TypeDefinition (
    TypeName ("Coordinates", _namePackage = Seq("draco", "base", "coordinates")),
    _domainAspect = DomainAspect (_elementTypeNames = Seq ("Spherical")))
  log.info(s"${coordinatesDomainDefinition.typeName.name} TypePackage: ${coordinatesDomainDefinition.typeName.namePackage}")

  val sphericalTypeName: TypeName = TypeName("Spherical", _namePackage = Seq("draco", "base", "coordinates"))
  val azimuthParameter: Parameter = Parameter ("_azimuth", "Radians", Json.Null)
  val elevationParameter: Parameter = Parameter ("_elevation", "Radians", Json.Null)
  val rangeParameter: Parameter = Parameter ("_range", "Meters", Json.Null)
  val sphericalParameters: Seq[Parameter] = Seq (azimuthParameter, elevationParameter, rangeParameter)
  val azimuthElement: TypeElement = Fixed ("azimuth", "Radians", Json.fromString("_azimuth"))
  val elevationElement: TypeElement = Fixed ("elevation", "Radians", Json.fromString("_elevation"))
  val rangeElement: TypeElement = Fixed ("elevation", "Meters", Json.fromString("_range"))
  val sphericalElements: Seq[TypeElement] = Seq (azimuthElement, elevationElement, rangeElement)
  val sphericalBoundsTypeName: TypeName = TypeName ("SphericalBounds", _namePackage = Seq("draco", "base", "orientable"))
  val lufParameter: Parameter = Parameter ("luf", "Spherical", Json.Null)
  val rlbParameter: Parameter = Parameter ("rlb", "Spherical", Json.Null)
  val leftBoundsElement: TypeElement = Fixed ("left", "Left[Radians]", Json.fromString("Left[Radians](_luf.azimuth)"))
  val rightBoundsElement: TypeElement = Fixed ("right", "Right[Radians]", Json.fromString("Right[Radians](_rlb.azimuth)"))
  val upperBoundsElement: TypeElement = Fixed ("upper", "Upper[Radians]", Json.fromString("Upper[Radians](_luf.elevation)"))
  val lowerBoundsElement: TypeElement = Fixed ("lower", "Lower[Radians]", Json.fromString("Lower[Radians](_rlb.elevation)"))
  val frontBoundsElement: TypeElement = Fixed ("front", "Front[Meters]", Json.fromString("Front[Meters](_luf.range)"))
  val backBoundsElement: TypeElement = Fixed ("back", "Back[Meters]", Json.fromString("Back[Meters](_rlb.range)"))
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
      _typeName = unitDomainDefinition.typeName,
      _dracoAspect = DracoAspect (_derivation = Seq (baseDomainDefinition.typeName))
    ),
    TypeDefinition(
      _typeName = measureTypeName,
      _dracoAspect = DracoAspect (_derivation = Seq (baseDomainDefinition.typeName))
    ),
    TypeDefinition(
      _typeName = angleTypeName,
      _dracoAspect = DracoAspect (_derivation = Seq (measureTypeName))
    ),
    TypeDefinition(
      _typeName = radiansTypeName,
      _dracoAspect = DracoAspect (_derivation = Seq (angleTypeName))
    ),
    TypeDefinition(
      _typeName = distanceTypeName,
      _dracoAspect = DracoAspect (_derivation = Seq (measureTypeName))
    ),
    TypeDefinition(
      _typeName = metersTypeName,
      _dracoAspect = DracoAspect (_derivation = Seq (distanceTypeName))
    ),
    TypeDefinition(
      _typeName = leftTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (measureTypeName),
        _factory = Factory(leftTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = rightTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (measureTypeName),
        _factory = Factory(rightTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = upperTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (measureTypeName),
        _factory = Factory(upperTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = lowerTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (measureTypeName),
        _factory = Factory(lowerTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = frontTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (measureTypeName),
        _factory = Factory(frontTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = backTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (measureTypeName),
        _factory = Factory(backTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = boundingBoxTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (orientableDomainDefinition.typeName),
        _elements = boundingBoxElements,
        _factory = Factory(boundingBoxTypeName.namePath, orientableParameters)
      )
    ),
    TypeDefinition(
      _typeName = sphericalTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (coordinatesDomainDefinition.typeName),
        _elements = sphericalElements,
        _factory = Factory(sphericalTypeName.namePath, sphericalParameters)
      )
    ),
    TypeDefinition(
      _typeName = sphericalBoundsTypeName,
      _dracoAspect = DracoAspect (
        _derivation = Seq (boundingBoxTypeName),
        _elements = sphericalBoundsElements,
        _factory = Factory(sphericalBoundsTypeName.namePath, sphericalBoundsParameters)
      )
    )
  )
  def testTypeDefinitionEncode(td: TypeDefinition): Unit = {
    val sourceString: String = td.asJson.spaces2
    log.info(s"${td.typeName.namePath}:")
    log.info(sourceString)
  }

  def testTypeDefinitionDecode(td: TypeDefinition): Unit = {
    val sourceString: String = td.asJson.spaces2
    val jsonObject = parser.parse(sourceString).getOrElse(Json.Null)
    val tdInstance: TypeDefinition = jsonObject.as[TypeDefinition].toTry.get
    log.info(s"${tdInstance.typeName.namePath}:")
    log.info(tdInstance.asJson.spaces2)
  }
  def testTypeDefinitionGenerate(td: TypeDefinition): Unit = {
    log.info(Generator.generate(td))
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
