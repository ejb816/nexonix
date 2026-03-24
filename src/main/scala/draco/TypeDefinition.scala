package draco

import io.circe.parser
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition {
  val typeName: TypeName
  val superDomain: TypeName
  val modules: Seq[TypeName]
  val derivation: Seq[TypeName]
  val elements: Seq[TypeElement]
  val factory: Factory
  val globalElements: Seq[BodyElement]
  val elementTypeNames: Seq[String]
  val source: TypeName
  val target: TypeName
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
  val values: Seq[Value]
  val pattern: Pattern
  val action: Action
  val messageAction: Action
  val signalAction: Action
}

object TypeDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "TypeDefinition",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("superDomain", "TypeName"),
      Fixed ("modules", "Seq[TypeName]"),
      Fixed ("derivation", "Seq[TypeName]"),
      Fixed ("elements", "Seq[TypeElement]"),
      Fixed ("factory", "Factory"),
      Fixed ("globalElements", "Seq[BodyElement]"),
      Fixed ("elementTypeNames", "Seq[String]"),
      Fixed ("source", "TypeName"),
      Fixed ("target", "TypeName"),
      Fixed ("variables", "Seq[Variable]"),
      Fixed ("conditions", "Seq[Condition]"),
      Fixed ("values", "Seq[Value]"),
      Fixed ("pattern", "Pattern"),
      Fixed ("action", "Action"),
      Fixed ("messageAction", "Action"),
      Fixed ("signalAction", "Action")
    ),
    _factory = Factory (
      "TypeDefinition",
      _parameters = Seq (
        Parameter ("typeName", "TypeName", ""),
        Parameter ("superDomain", "TypeName", "TypeName.Null"),
        Parameter ("modules", "Seq[TypeName]", "Seq.empty"),
        Parameter ("derivation", "Seq[TypeName]", "Seq.empty"),
        Parameter ("elements", "Seq[TypeElement]", "Seq.empty"),
        Parameter ("factory", "Factory", "Factory.Null"),
        Parameter ("globalElements", "Seq[BodyElement]", "Seq.empty"),
        Parameter ("elementTypeNames", "Seq[String]", "Seq.empty"),
        Parameter ("source", "TypeName", "TypeName.Null"),
        Parameter ("target", "TypeName", "TypeName.Null"),
        Parameter ("variables", "Seq[Variable]", "Seq.empty"),
        Parameter ("conditions", "Seq[Condition]", "Seq.empty"),
        Parameter ("values", "Seq[Value]", "Seq.empty"),
        Parameter ("pattern", "Pattern", "Pattern.Null"),
        Parameter ("action", "Action", "Action.Null"),
        Parameter ("messageAction", "Action", "Action.Null"),
        Parameter ("signalAction", "Action", "Action.Null")
      )
    )
  )
  lazy val typeInstance: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)

  def load (typeName: TypeName) : TypeDefinition = {
    val stream = getClass.getResourceAsStream(typeName.resourcePath)
    if (stream == null) return TypeDefinition(typeName)
    val source = scala.io.Source.fromInputStream(stream)
    try {
      val json = source.mkString
      parser.parse(json).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition(typeName))
    } finally source.close()
  }
  def apply (
              _typeName: TypeName,
              _superDomain: TypeName = TypeName.Null,
              _modules: Seq[TypeName] = Seq.empty,
              _derivation: Seq[TypeName] = Seq.empty,
              _elements: Seq[TypeElement] = Seq.empty,
              _factory: Factory = Factory.Null,
              _globalElements: Seq[BodyElement] = Seq.empty,
              _elementTypeNames: Seq[String] = Seq.empty,
              _source: TypeName = TypeName.Null,
              _target: TypeName = TypeName.Null,
              _variables: Seq[Variable] = Seq.empty,
              _conditions: Seq[Condition] = Seq.empty,
              _values: Seq[Value] = Seq.empty,
              _pattern: Pattern = null,
              _action: Action = null,
              _messageAction: Action = null,
              _signalAction: Action = null
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val superDomain: TypeName = _superDomain
      override val modules: Seq[TypeName] = _modules
      override val derivation: Seq[TypeName] = _derivation
      override val elements: Seq[TypeElement] = _elements
      override val factory: Factory = _factory
      override val globalElements: Seq[BodyElement] = _globalElements
      override val elementTypeNames: Seq[String] = _elementTypeNames
      override val source: TypeName = _source
      override val target: TypeName = _target
      override val variables: Seq[Variable] = _variables
      override val conditions: Seq[Condition] = _conditions
      override val values: Seq[Value] = _values
      override lazy val pattern: Pattern = if (_pattern != null) _pattern else Pattern.Null
      override lazy val action: Action = if (_action != null) _action else Action.Null
      override lazy val messageAction: Action = if (_messageAction != null) _messageAction else Action.Null
      override lazy val signalAction: Action = if (_signalAction != null) _signalAction else Action.Null
    }
  }
  // Encode a TypeDefinition
  lazy implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    val fields = Seq(
      Some("typeName" -> td.typeName.asJson),
      if (td.superDomain.name.nonEmpty) Some("superDomain" -> td.superDomain.asJson) else None,
      if (td.modules.nonEmpty) Some("modules" -> td.modules.asJson) else None,
      if (td.derivation.nonEmpty) Some("derivation" -> td.derivation.asJson) else None,
      if (td.elements.nonEmpty) Some("elements" -> td.elements.asJson) else None,
      if (td.factory.valueType.nonEmpty) Some("factory" -> td.factory.asJson) else None,
      if (td.globalElements.nonEmpty) Some("globalElements" -> td.globalElements.asJson) else None,
      if (td.elementTypeNames.nonEmpty) Some("elementTypeNames" -> td.elementTypeNames.asJson) else None,
      if (td.source.name.nonEmpty) Some("source" -> td.source.asJson) else None,
      if (td.target.name.nonEmpty) Some("target" -> td.target.asJson) else None,
      if (td.variables.nonEmpty) Some("variables" -> td.variables.asJson) else None,
      if (td.conditions.nonEmpty) Some("conditions" -> td.conditions.asJson) else None,
      if (td.values.nonEmpty) Some("values" -> td.values.asJson) else None,
      if (td.pattern.variables.nonEmpty || td.pattern.conditions.nonEmpty) Some("pattern" -> td.pattern.asJson) else None,
      if (td.action.body.nonEmpty) Some("action" -> td.action.asJson) else None,
      if (td.messageAction.body.nonEmpty) Some("messageAction" -> td.messageAction.asJson) else None,
      if (td.signalAction.body.nonEmpty) Some("signalAction" -> td.signalAction.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  lazy implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName    <- cursor.downField("typeName").as[TypeName]
      _superDomain <- cursor.downField("superDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _modules     <- cursor.downField("modules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _derivation  <- cursor.downField("derivation").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _elements    <- cursor.downField("elements").as[Option[Seq[TypeElement]]].map(_.getOrElse(Seq.empty))
      _factory     <- cursor.downField("factory").as[Option[Factory]].map(_.getOrElse(Factory.Null))
      _globalElements <- cursor.downField("globalElements").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
      _elementTypeNames <- cursor.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _source      <- cursor.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target      <- cursor.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _variables   <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions  <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values      <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _pattern     <- cursor.downField("pattern").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
      _action      <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
      _messageAction <- cursor.downField("messageAction").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signalAction  <- cursor.downField("signalAction").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield TypeDefinition (
      _typeName,
      _superDomain,
      _modules,
      _derivation,
      _elements,
      _factory,
      _globalElements,
      _elementTypeNames,
      _source,
      _target,
      _variables,
      _conditions,
      _values,
      _pattern,
      _action,
      _messageAction,
      _signalAction
    )
  }
  lazy val Null: TypeDefinition = TypeDefinition (TypeName.Null)
}
