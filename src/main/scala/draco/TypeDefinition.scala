package draco

import io.circe.parser
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition {
  val typeName: TypeName
  val modules: Seq[TypeName]
  val derivation: Seq[TypeName]
  val elements: Seq[TypeElement]
  val factory: Factory
  val globalElements: Seq[BodyElement]
}

object TypeDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "TypeDefinition",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("modules", "Seq[TypeName]"),
      Fixed ("derivation", "Seq[TypeName]"),
      Fixed ("elements", "Seq[TypeElement]"),
      Fixed ("factory", "Factory"),
      Fixed ("globalElements", "Seq[BodyElement]")
    ),
    _factory = Factory (
      "TypeDefinition",
      _parameters = Seq (
        Parameter ("typeName", "TypeName", ""),
        Parameter ("modules", "Seq[TypeName]", "Seq.empty"),
        Parameter ("derivation", "Seq[TypeName]", "Seq.empty"),
        Parameter ("elements", "Seq[TypeElement]", "Seq.empty"),
        Parameter ("factory", "Factory", "Factory.Null"),
        Parameter ("globalElements", "Seq[BodyElement]", "Seq.empty")
      )
    )
  )
  lazy val typeInstance: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)

  def load (typeName: TypeName) : TypeDefinition = {
    val sourceContent = SourceContent(Generator.main.sourceRoot, typeName.resourcePath)
    val sourceJSON: Json = parser.parse(sourceContent.sourceString).getOrElse(TypeDefinition (typeName).asJson)
    sourceJSON.as[TypeDefinition].getOrElse(Null)
  }
  def apply (
              _typeName: TypeName,
              _modules: Seq[TypeName] = Seq.empty,
              _derivation: Seq[TypeName] = Seq.empty,
              _elements: Seq[TypeElement] = Seq.empty,
              _factory: Factory = Factory.Null,
              _globalElements: Seq[BodyElement] = Seq.empty
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val modules: Seq[TypeName] = _modules
      override val derivation: Seq[TypeName] = _derivation
      override val elements: Seq[TypeElement] = _elements
      override val factory: Factory = _factory
      override val globalElements: Seq[BodyElement] = _globalElements
    }
  }
  // Encode a TypeDefinition
  lazy implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    val fields = Seq(
      Some("typeName" -> td.typeName.asJson),
      if (td.modules.nonEmpty) Some("modules" -> td.modules.asJson) else None,
      if (td.derivation.nonEmpty) Some("derivation" -> td.derivation.asJson) else None,
      if (td.elements.nonEmpty) Some("elements" -> td.elements.asJson) else None,
      if (td.factory.valueType.nonEmpty) Some("factory" -> td.factory.asJson) else None,
      if (td.globalElements.nonEmpty) Some("globalElements" -> td.globalElements.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  lazy implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName    <- cursor.downField("typeName").as[TypeName]
      _modules     <- cursor.downField("modules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _derivation  <- cursor.downField("derivation").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _elements    <- cursor.downField("elements").as[Option[Seq[TypeElement]]].map(_.getOrElse(Seq.empty))
      _factory     <- cursor.downField("factory").as[Option[Factory]].map(_.getOrElse(Factory.Null))
      _globalElements <- cursor.downField("globalElements").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
    } yield TypeDefinition (
      _typeName,
      _modules,
      _derivation,
      _elements,
      _factory,
      _globalElements
    )
  }
  lazy val Null: TypeDefinition = TypeDefinition (TypeName.Null)
}
