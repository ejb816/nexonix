package draco

import io.circe.parser
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition extends Draco {
  val typeName: TypeName
  val typeParameters: Seq[String]
  val dependsOn: Seq[TypeName]
  val derivesFrom: Seq[TypeName]
  val members: Seq[Member]
  val parameters: Seq[Member]
  val rules: Seq[TypeName]
}

object TypeDefinition extends App {
  def load (typeName: TypeName) : TypeDefinition = {
    val resourcePath = typeName.resourcePath
    val sourceContent = SourceContent(resourcePath)
    val sourceJSON: Json = parser.parse(sourceContent.sourceString).getOrElse(TypeDefinition (typeName).asJson)
    sourceJSON.as[TypeDefinition].getOrElse(Null)
  }
  def apply (
              _typeName: TypeName,
              _typeParameters: Seq[String] = Seq[String](),
              _dependsOn: Seq[TypeName] = Seq[TypeName](),
              _derivesFrom: Seq[TypeName] = Seq[TypeName](),
              _members: Seq[Member] = Seq[Member](),
              _parameters: Seq[Member] = Seq[Member](),
              _rules: Seq[TypeName] = Seq[TypeName]()
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val typeParameters: Seq[String] = _typeParameters
      override val dependsOn: Seq[TypeName] = _dependsOn
      override val derivesFrom: Seq[TypeName] = _derivesFrom
      override val members: Seq[Member] = _members
      override val parameters: Seq[Member] = _parameters
      override val rules: Seq[TypeName] = _rules
    }
  }
  // Encode a TypeDefinition
  lazy implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    Json.obj(
      "typeName"       -> td.typeName.asJson,       // TypeName
      "typeParameters" -> td.typeParameters.asJson, // Seq[String]
      "dependsOn"      -> td.dependsOn.asJson,      // Seq[TypeName]
      "derivesFrom"    -> td.derivesFrom.asJson,    // Seq[TypeName]
      "members"        -> td.members.asJson,        // Seq[Member]
      "parameters"     -> td.parameters.asJson,     // Seq[Member]
      "rules"          -> td.rules.asJson           // Seq[Rule]
    )
  }

  lazy implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName        <- cursor.downField("typeName").as[TypeName]
      _typeParameters  <- cursor.downField("typeParameters").as[Seq[String]]
      _dependsOn       <- cursor.downField("dependsOn").as[Seq[TypeName]]
      _derivesFrom     <- cursor.downField("derivesFrom").as[Seq[TypeName]]
      _members         <- cursor.downField("members").as[Seq[Member]]
      _parameters      <- cursor.downField("parameters").as[Seq[Member]]
      _rules           <- cursor.downField("rules").as[Seq[TypeName]]
    } yield TypeDefinition (
      _typeName,
      _typeParameters,
      _dependsOn,
      _derivesFrom,
      _members,
      _parameters,
      _rules
    )
  }
  lazy val Null: TypeDefinition = new TypeDefinition {
    override val typeName: TypeName = TypeName.Null
    override val typeParameters: Seq[String] = Seq()
    override val dependsOn: Seq[TypeName] = Seq()
    override val derivesFrom: Seq[TypeName] = Seq()
    override val members: Seq[Member] = Seq()
    override val parameters: Seq[Member] = Seq()
    override val rules: Seq[TypeName] = Seq()
  }
}
