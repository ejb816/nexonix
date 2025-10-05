package draco

import io.circe.parser
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition {
  val typeName: TypeName
  val typeParameters: Seq[String]
  val moduleElements: Seq[TypeName]
  val dependsOn: Seq[TypeName]
  val derivesFrom: Seq[TypeName]
  val elements: Seq[TypeElement]
  val parameters: Seq[TypeElement]
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
              _moduleElements: Seq[TypeName] = Seq[TypeName](),
              _dependsOn: Seq[TypeName] = Seq[TypeName](),
              _derivesFrom: Seq[TypeName] = Seq[TypeName](),
              _elements: Seq[TypeElement] = Seq[TypeElement](),
              _parameters: Seq[TypeElement] = Seq[TypeElement](),
              _rules: Seq[TypeName] = Seq[TypeName]()
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val typeParameters: Seq[String] = _typeParameters
      override val moduleElements: Seq[TypeName] = _moduleElements
      override val dependsOn: Seq[TypeName] = _dependsOn
      override val derivesFrom: Seq[TypeName] = _derivesFrom
      override val elements: Seq[TypeElement] = _elements
      override val parameters: Seq[TypeElement] = _parameters
      override val rules: Seq[TypeName] = _rules
    }
  }
  // Encode a TypeDefinition
  lazy implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    Json.obj(
      "typeName"       -> td.typeName.asJson,       // TypeName
      "typeParameters" -> td.typeParameters.asJson, // Seq[String]
      "moduleElements" -> td.moduleElements.asJson, // Seq[TypeName]
      "dependsOn"      -> td.dependsOn.asJson,      // Seq[TypeName]
      "derivesFrom"    -> td.derivesFrom.asJson,    // Seq[TypeName]
      "elements"       -> td.elements.asJson,       // Seq[TypeElement]
      "parameters"     -> td.parameters.asJson,     // Seq[TypeElement]
      "rules"          -> td.rules.asJson           // Seq[TypeName]
    )
  }

  lazy implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName        <- cursor.downField("typeName").as[TypeName]
      _typeParameters  <- cursor.downField("typeParameters").as[Seq[String]]
      _moduleElements  <- cursor.downField("moduleElements").as[Seq[TypeName]]
      _dependsOn       <- cursor.downField("dependsOn").as[Seq[TypeName]]
      _derivesFrom     <- cursor.downField("derivesFrom").as[Seq[TypeName]]
      _elements        <- cursor.downField("elements").as[Seq[TypeElement]]
      _parameters      <- cursor.downField("parameters").as[Seq[TypeElement]]
      _rules           <- cursor.downField("rules").as[Seq[TypeName]]
    } yield TypeDefinition (
      _typeName,
      _typeParameters,
      _moduleElements,
      _dependsOn,
      _derivesFrom,
      _elements,
      _parameters,
      _rules
    )
  }
  lazy val Null: TypeDefinition = new TypeDefinition {
    override val typeName: TypeName = TypeName.Null
    override val typeParameters: Seq[String] = Seq()
    override val moduleElements: Seq[TypeName] = Seq()
    override val dependsOn: Seq[TypeName] = Seq()
    override val derivesFrom: Seq[TypeName] = Seq()
    override val elements: Seq[TypeElement] = Seq()
    override val parameters: Seq[TypeElement] = Seq()
    override val rules: Seq[TypeName] = Seq()
  }
}
