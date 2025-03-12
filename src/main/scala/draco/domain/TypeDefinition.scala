package draco.domain

import draco.domain
import io.circe._
import io.circe.syntax._

sealed trait TypeDefinition {
  val typeName: TypeName
  val typeParameters: Seq[String]
  val dependsOn: Seq[TypeName]
  val derivesFrom: Seq[TypeName]
  val members: Seq[Member]
  val parameters: Seq[Parameter]
  val rules: Seq[Rule]
}

object TypeDefinition {
  val NULL: TypeDefinition = TypeDefinition (
    _typeName = TypeName(_name = "",
    _namePackage = Seq())
  )
  def apply (
              _typeName: TypeName,
              _typeParameters: Seq[String] = Seq[String](),
              _dependsOn: Seq[TypeName] = Seq[TypeName](),
              _derivesFrom: Seq[TypeName] = Seq[TypeName](),
              _members: Seq[Member] = Seq[Member](),
              _parameters: Seq[Parameter] = Seq[Parameter](),
              _rules: Seq[Rule] = Seq[Rule]()
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val typeParameters: Seq[String] = _typeParameters
      override val dependsOn: Seq[TypeName] = _dependsOn
      override val derivesFrom: Seq[TypeName] = _derivesFrom
      override val members: Seq[Member] = _members
      override val parameters: Seq[Parameter] = _parameters
      override val rules: Seq[Rule] = _rules
    }
  }
  // Encode a TypeDefinition
  implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    Json.obj(
      "typeName"       -> td.typeName.asJson,
      "typeParameters" -> td.typeParameters.asJson,
      "dependsOn"      -> td.dependsOn.asJson,   // Seq[TypeName]
      "derivesFrom"    -> td.derivesFrom.asJson, // Seq[TypeName]
      "members"        -> td.members.asJson,     // Seq[Member]
      "parameters"     -> td.parameters.asJson,  // Seq[Parameter]
      "rules"          -> td.rules.asJson        // Seq[Rule]
    )
  }

  implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName        <- cursor.downField("typeName").as[TypeName]
      _typeParameters  <- cursor.downField("typeParameters").as[Seq[String]]
      _dependsOn       <- cursor.downField("dependsOn").as[Seq[TypeName]]
      _derivesFrom     <- cursor.downField("derivesFrom").as[Seq[TypeName]]
      _members         <- cursor.downField("members").as[Seq[Member]]
      _parameters      <- cursor.downField("parameters").as[Seq[Parameter]]
      _rules           <- cursor.downField("rules").as[Seq[Rule]]
    } yield domain.TypeDefinition (
      _typeName,
      _typeParameters,
      _dependsOn,
      _derivesFrom,
      _members,
      _parameters,
      _rules
    )
  }
}