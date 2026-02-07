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
  val typeGlobals: Seq[BodyElement]
  val rules: Seq[TypeName]
}

object TypeDefinition extends App {
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
              _typeGlobals: Seq[BodyElement] = Seq.empty,
              _rules: Seq[TypeName] = Seq.empty
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val modules: Seq[TypeName] = _modules
      override val derivation: Seq[TypeName] = _derivation
      override val elements: Seq[TypeElement] = _elements
      override val factory: Factory = _factory
      override val typeGlobals: Seq[BodyElement] = _typeGlobals
      override val rules: Seq[TypeName] = _rules
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
      if (td.typeGlobals.nonEmpty) Some("typeGlobals" -> td.typeGlobals.asJson) else None,
      if (td.rules.nonEmpty) Some("rules" -> td.rules.asJson) else None
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
      _typeGlobals <- cursor.downField("typeGlobals").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
      _rules       <- cursor.downField("rules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
    } yield TypeDefinition (
      _typeName,
      _modules,
      _derivation,
      _elements,
      _factory,
      _typeGlobals,
      _rules
    )
  }
  lazy val Null: TypeDefinition = new TypeDefinition {
    override val typeName: TypeName = TypeName.Null
    override val modules: Seq[TypeName] = Seq.empty
    override val derivation: Seq[TypeName] = Seq.empty
    override val elements: Seq[TypeElement] = Seq.empty
    override val factory: Factory = Factory.Null
    override val typeGlobals: Seq[BodyElement] = Seq.empty
    override val rules: Seq[TypeName] = Seq.empty
  }
  lazy val Default: TypeDefinition = TypeDefinition (
    TypeName (
      _name = "NewUnnamedType",
      _namePackage = Seq ("new", "unknown", "package", "name")
    )
  )
}
