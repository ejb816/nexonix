package draco

import io.circe.syntax.EncoderOps

sealed trait RuleDefinition {
  val typeName: TypeName
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
  val values: Seq[Value]
  val pattern: Pattern
  val action: Action
}

object RuleDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "RuleDefinition",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("variables", "Seq[Variable]"),
      Fixed ("conditions", "Seq[Condition]"),
      Fixed ("values", "Seq[Value]"),
      Fixed ("action", "Action")
    ),
    _factory = Factory (
      "RuleDefinition",
      _parameters = Seq (
        Parameter ("typeName", "TypeName", ""),
        Parameter ("variables", "Seq[Variable]", "Seq.empty"),
        Parameter ("conditions", "Seq[Condition]", "Seq.empty"),
        Parameter ("values", "Seq[Value]", "Seq.empty"),
        Parameter ("action", "Action", "Action.Null")
      )
    )
  )
  lazy val typeInstance: Type[RuleDefinition] = Type[RuleDefinition] (typeDefinition)

  def apply (
              _typeName: TypeName,
              _variables: Seq[Variable] = Seq.empty,
              _conditions: Seq[Condition] = Seq.empty,
              _values: Seq[Value] = Seq.empty,
              _pattern: Pattern = Pattern.Null,
              _action: Action = Action.Null
            ) : RuleDefinition = {
    new RuleDefinition {
      override val typeName: TypeName = _typeName
      override val variables: Seq[Variable] = _variables
      override val conditions: Seq[Condition] = _conditions
      override val values: Seq[Value] = _values
      override val pattern: Pattern = _pattern
      override val action: Action = _action
    }
  }

  // Encode a RuleDefinition.json
  implicit lazy val encoder: io.circe.Encoder[RuleDefinition] = io.circe.Encoder.instance { r =>
    val fields = Seq (
      Some("typeName" -> r.typeName.asJson),
      if (r.variables.nonEmpty) Some("variables" -> r.variables.asJson) else None,
      if (r.conditions.nonEmpty) Some("conditions" -> r.conditions.asJson) else None,
      if (r.values.nonEmpty) Some("values" -> r.values.asJson) else None,
      if (r.action.body.nonEmpty) Some("action" -> r.action.asJson) else None
    ).flatten

    io.circe.Json.obj(fields: _*)
  }

  implicit lazy val decoder: io.circe.Decoder[RuleDefinition] = io.circe.Decoder.instance { cursor =>
    for {
      _typeName   <- cursor.downField("typeName").as[TypeName]
      _variables  <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values     <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _pattern     <- cursor.downField("pattern").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
      _action     <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield RuleDefinition (
      _typeName,
      _variables,
      _conditions,
      _values,
      _pattern,
      _action
    )
  }
  lazy val Null: RuleDefinition = RuleDefinition (
    _typeName = TypeName.Null,
    _variables = Seq.empty,
    _conditions = Seq.empty,
    _values = Seq.empty,
    _action = Action.Null
  )
}
