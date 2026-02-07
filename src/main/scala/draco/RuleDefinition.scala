package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait RuleDefinition {
  val typeName: TypeName
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
  val values: Seq[Value]
  val action: Action
}

object RuleDefinition {
  def apply (
              _typeName: TypeName,
              _variables: Seq[Variable] = Seq.empty,
              _conditions: Seq[Condition] = Seq.empty,
              _values: Seq[Value] = Seq.empty,
              _action: Action = Action.Null
            ) : RuleDefinition = {
    new RuleDefinition {
      override val typeName: TypeName = _typeName
      override val variables: Seq[Variable] = _variables
      override val conditions: Seq[Condition] = _conditions
      override val values: Seq[Value] = _values
      override val action: Action = _action
    }
  }

  // Encode a RuleDefinition.json
  implicit val encoder: Encoder[RuleDefinition] = Encoder.instance { r =>
    val fields = Seq(
      Some("typeName" -> r.typeName.asJson),
      if (r.variables.nonEmpty) Some("variables" -> r.variables.asJson) else None,
      if (r.conditions.nonEmpty) Some("conditions" -> r.conditions.asJson) else None,
      if (r.values.nonEmpty) Some("values" -> r.values.asJson) else None,
      if (r.action.body.nonEmpty) Some("action" -> r.action.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  implicit val decoder: Decoder[RuleDefinition] = Decoder.instance { cursor =>
    for {
      _typeName   <- cursor.downField("typeName").as[TypeName]
      _variables  <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values     <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _action     <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield RuleDefinition (
      _typeName,
      _variables,
      _conditions,
      _values,
      _action
    )
  }
  val Null: RuleDefinition = RuleDefinition (
    _typeName = TypeName.Null,
    _variables = Seq.empty,
    _conditions = Seq.empty,
    _values = Seq.empty,
    _action = Action.Null
  )
}
