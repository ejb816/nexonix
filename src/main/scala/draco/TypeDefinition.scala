package draco

import io.circe.parser
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition {
  val typeName: TypeName
  val typeParameters: Seq[String]
  val typeModule: Seq[TypeName]
  val typeInternals: Seq[TypeName]
  val dependsOn: Seq[TypeName]
  val derivesFrom: Seq[TypeName]
  val members: Seq[Member]
  val parameters: Seq[Parameter]
  val rules: Seq[TypeName]
}

object TypeDefinition extends App {
  def load(typeName: TypeName) : TypeDefinition = {

    val resourcePath = typeName.resourcePath + ".json"
    val sourceContent = SourceContent(resourcePath)
    val sourceJSON: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    sourceJSON.as[TypeDefinition].getOrElse(Null)
  }

  def load (rootTypeName: TypeName, elementNames: Seq[String]) : Seq[TypeDefinition] = {
    val elementTypeNames: Seq[TypeName] = elementNames.map(name => {
      TypeName(name, rootTypeName.typePackage)})
    elementTypeNames.map(typeName => load(typeName))
  }
  def apply (
              _typeName: TypeName,
              _typeParameters: Seq[String] = Seq[String](),
              _typeModule: Seq[TypeName] = Seq[TypeName](),
              _typeInternals: Seq[TypeName] = Seq[TypeName](),
              _dependsOn: Seq[TypeName] = Seq[TypeName](),
              _derivesFrom: Seq[TypeName] = Seq[TypeName](),
              _members: Seq[Member] = Seq[Member](),
              _parameters: Seq[Parameter] = Seq[Parameter](),
              _rules: Seq[TypeName] = Seq[TypeName]()
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val typeParameters: Seq[String] = _typeParameters
      override val typeModule: Seq[TypeName] = _typeModule
      override val typeInternals: Seq[TypeName] = _typeInternals
      override val dependsOn: Seq[TypeName] = _dependsOn
      override val derivesFrom: Seq[TypeName] = _derivesFrom
      override val members: Seq[Member] = _members
      override val parameters: Seq[Parameter] = _parameters
      override val rules: Seq[TypeName] = _rules
    }
  }
  // Encode a TypeDefinition
  lazy implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    Json.obj(
      "typeName"       -> td.typeName.asJson,       // TypeName
      "typeParameters" -> td.typeParameters.asJson, // Seq[String]
      "typeGroup"      -> td.typeModule.asJson,      // Seq[TypeName]
      "typeInternals"  -> td.typeInternals.asJson,  // Seq[TypeName]
      "dependsOn"      -> td.dependsOn.asJson,      // Seq[TypeName]
      "derivesFrom"    -> td.derivesFrom.asJson,    // Seq[TypeName]
      "members"        -> td.members.asJson,        // Seq[Member]
      "parameters"     -> td.parameters.asJson,     // Seq[Parameter]
      "rules"          -> td.rules.asJson           // Seq[Rule]
    )
  }

  lazy implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName        <- cursor.downField("typeName").as[TypeName]
      _typeParameters  <- cursor.downField("typeParameters").as[Seq[String]]
      _typeGroup       <- cursor.downField("typeGroup").as[Seq[TypeName]]
      _typeInternals   <- cursor.downField("typeInternals").as[Seq[TypeName]]
      _dependsOn       <- cursor.downField("dependsOn").as[Seq[TypeName]]
      _derivesFrom     <- cursor.downField("derivesFrom").as[Seq[TypeName]]
      _members         <- cursor.downField("members").as[Seq[Member]]
      _parameters      <- cursor.downField("parameters").as[Seq[Parameter]]
      _rules           <- cursor.downField("rules").as[Seq[TypeName]]
    } yield TypeDefinition (
      _typeName,
      _typeParameters,
      _typeGroup,
      _typeInternals,
      _dependsOn,
      _derivesFrom,
      _members,
      _parameters,
      _rules
    )
  }
  lazy val Null: TypeDefinition = TypeDefinition (_typeName = TypeName.Null)
  println("draco.TypeDefinition.Null:")
  println(TypeDefinition.Null.asJson.spaces2)
  println(
    s"""draco.TypeDefinition (
       |  TypeName ("TypeDefinition", "Draco"),
       |  _members: Seq(
       |    Fixed ("typeName", "TypeName", ""),
       |    Fixed ("typeParameters", "Seq[String]", ""),
       |    Fixed ("typeGroup", "Seq[TypeName]", ""),
       |    Fixed ("typeInternals", "Seq[TypeName]", ""),
       |    Fixed ("dependsOn", "Seq[TypeName]", ""),
       |    Fixed ("derivesFrom", "Seq[TypeName]", ""),
       |    Fixed ("members", "Seq[Member]", ""),
       |    Fixed ("parameters", "Seq[Parameter]", ""),
       |    Fixed ("rules", "Seq[TypeName]", "")
       |  )
       |):""".stripMargin)
  println(TypeDefinition (
    TypeName ("TypeDefinition", TypePackage("Draco")),
    _members = Seq(
      Fixed ("typeName", "TypeName", ""),
      Fixed ("typeParameters", "Seq[String]", ""),
      Fixed ("typeGroup", "Seq[TypeName]", ""),
      Fixed ("typeInternals", "Seq[TypeName]", ""),
      Fixed ("dependsOn", "Seq[TypeName]", ""),
      Fixed ("derivesFrom", "Seq[TypeName]", ""),
      Fixed ("members", "Seq[Member]", ""),
      Fixed ("parameters", "Seq[Parameter]", ""),
      Fixed ("rules", "Seq[TypeName]", "")
    )
  ).asJson.spaces2)
}