package org.nexonix.json

import draco.{Fixed, Generator, Member, DomainName, Parameter, TypeDefinition, TypeName}
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

  val orientableTypeParameters: Seq[String] = Seq ("T")
  val valueParameter: Parameter = Parameter ("value", "T")
  val leftTypeName: TypeName = TypeName ("Left", orientableDomainName.typeName.fullName)
  val rightTypeName: TypeName = TypeName ("Right", orientableDomainName.typeName.fullName)
  val upperTypeName: TypeName = TypeName ("Upper", orientableDomainName.typeName.fullName)
  val lowerTypeName: TypeName = TypeName ("Lower", orientableDomainName.typeName.fullName)
  val frontTypeName: TypeName = TypeName ("Front", orientableDomainName.typeName.fullName)
  val backTypeName: TypeName = TypeName ("Back", orientableDomainName.typeName.fullName)
  val orientableParameters: Seq[Parameter] = Seq(valueParameter)
  val boundingBoxTypeName: TypeName = TypeName("BoundingBox", orientableDomainName.typeName.fullName)
  val leftMember: Member = Fixed ("left", "Left[Radians]")
  val rightMember: Member = Fixed ("right", "Right[Radians]")
  val upperMember: Member = Fixed ("upper", "Upper[Radians]")
  val lowerMember: Member = Fixed ("lower", "Lower[Radians]")
  val frontMember: Member = Fixed ("front", "Front[Meters]")
  val backMember: Member = Fixed ("back", "Back[Meters]")
  val boundingBoxMembers: Seq[Member] = Seq(
    leftMember,
    rightMember,
    upperMember,
    lowerMember,
    frontMember,
    backMember
  )
  val coordinatesDomainName: DomainName = DomainName (
    TypeName ("Coordinates", baseDomainName.typeName.fullName),
    Seq ("Spherical"))
  println(s"${coordinatesDomainName.typeName.name} TypePackage: ${coordinatesDomainName.typeName.namePackage}")

  val sphericalTypeName: TypeName = TypeName("Spherical", coordinatesDomainName.typeName.fullName)
  val azimuthParameter: Parameter = Parameter ("_azimuth", "Radians")
  val elevationParameter: Parameter = Parameter ("_elevation", "Radians")
  val rangeParameter: Parameter = Parameter ("_range", "Meters")
  val sphericalParameters: Seq[Parameter] = Seq (azimuthParameter, elevationParameter, rangeParameter)
  val azimuthMember: Member = Fixed ("azimuth", "Radians", "_azimuth")
  val elevationMember: Member = Fixed ("elevation", "Radians", "_elevation")
  val rangeMember: Member = Fixed ("elevation", "Meters", "_range")
  val sphericalMembers: Seq[Member] = Seq (azimuthMember, elevationMember, rangeMember)
  val sphericalBoundsTypeName: TypeName = TypeName ("SphericalBounds", orientableDomainName.typeName.fullName)
  val lufParameter: Parameter = Parameter ("luf", "Spherical")
  val rlbParameter: Parameter = Parameter ("rlb", "Spherical")
  val leftBoundsMember: Member = Fixed ("left", "Left[Radians]", "Left[Radians](_luf.azimuth)")
  val rightBoundsMember: Member = Fixed ("right", "Right[Radians]", "Right[Radians](_rlb.azimuth)")
  val upperBoundsMember: Member = Fixed ("upper", "Upper[Radians]", "Upper[Radians](_luf.elevation)")
  val lowerBoundsMember: Member = Fixed ("lower", "Lower[Radians]", "Lower[Radians](_rlb.elevation)")
  val frontBoundsMember: Member = Fixed ("front", "Front[Meters]", "Front[Meters](_luf.range)")
  val backBoundsMember: Member = Fixed ("back", "Back[Meters]", "Back[Meters](_rlb.range)")
  val sphericalBoundsParameters: Seq[Parameter] = Seq (lufParameter, rlbParameter)
  val sphericalBoundsMembers: Seq[Member] = Seq (
    leftBoundsMember,
    rightBoundsMember,
    upperBoundsMember,
    lowerBoundsMember,
    frontBoundsMember,
    backBoundsMember
  )

  val tdList: Seq[TypeDefinition] = Seq[TypeDefinition] (
    TypeDefinition(
      _typeName = unitDomainName.typeName,
      _derivesFrom = Seq (baseDomainName.typeName)
    ),
    TypeDefinition(
      _typeName = measureTypeName,
      _derivesFrom = Seq (baseDomainName.typeName)
    ),
    TypeDefinition(
      _typeName = angleTypeName,
      _derivesFrom = Seq (measureTypeName)
    ),
    TypeDefinition(
      _typeName = radiansTypeName,
      _derivesFrom = Seq (angleTypeName)
    ),
    TypeDefinition(
      _typeName = distanceTypeName,
      _derivesFrom = Seq (measureTypeName)
    ),
    TypeDefinition(
      _typeName = metersTypeName,
      _derivesFrom = Seq (distanceTypeName)
    ),
    TypeDefinition(
      _typeName = leftTypeName,
      _typeParameters = orientableTypeParameters,
      _derivesFrom = Seq (measureTypeName),
      _parameters = orientableParameters
    ),
    TypeDefinition(
      _typeName = rightTypeName,
      _typeParameters = orientableTypeParameters,
      _derivesFrom = Seq (measureTypeName),
      _parameters = orientableParameters
    ),
    TypeDefinition(
      _typeName = upperTypeName,
      _typeParameters = orientableTypeParameters,
      _derivesFrom = Seq (measureTypeName),
      _parameters = orientableParameters
    ),
    TypeDefinition(
      _typeName = lowerTypeName,
      _typeParameters = orientableTypeParameters,
      _derivesFrom = Seq (measureTypeName),
      _parameters = orientableParameters
    ),
    TypeDefinition(
      _typeName = frontTypeName,
      _typeParameters = orientableTypeParameters,
      _derivesFrom = Seq (measureTypeName),
      _parameters = orientableParameters
    ),
    TypeDefinition(
      _typeName = backTypeName,
      _typeParameters = orientableTypeParameters,
      _derivesFrom = Seq (measureTypeName),
      _parameters = orientableParameters
    ),
    TypeDefinition(
      _typeName = boundingBoxTypeName,
      _dependsOn = Seq (metersTypeName, radiansTypeName),
      _derivesFrom = Seq (orientableDomainName.typeName),
      _parameters = orientableParameters,
      _members = boundingBoxMembers
    ),
    TypeDefinition(
      _typeName = sphericalTypeName,
      _dependsOn = Seq (metersTypeName, radiansTypeName),
      _derivesFrom = Seq (coordinatesDomainName.typeName),
      _parameters = sphericalParameters,
      _members = sphericalMembers
    ),
    TypeDefinition(
      _typeName = sphericalBoundsTypeName,
      _dependsOn = Seq (sphericalTypeName, metersTypeName, radiansTypeName),
      _derivesFrom = Seq (boundingBoxTypeName),
      _parameters = sphericalBoundsParameters,
      _members = sphericalBoundsMembers
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
