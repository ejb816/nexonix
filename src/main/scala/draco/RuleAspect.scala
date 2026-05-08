package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait RuleAspect extends Extensible {
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
  val values: Seq[Value]
  val pattern: Pattern
  val action: Action
}

object RuleAspect {
  def apply(
             _variables: Seq[Variable] = Seq.empty,
             _conditions: Seq[Condition] = Seq.empty,
             _values: Seq[Value] = Seq.empty,
             _pattern: Pattern = Pattern.Null,
             _action: Action = Action.Null
           ): RuleAspect = new RuleAspect {
    override val variables: Seq[Variable] = _variables
    override val conditions: Seq[Condition] = _conditions
    override val values: Seq[Value] = _values
    override lazy val pattern: Pattern = if (_pattern != null) _pattern else Pattern.Null
    override lazy val action: Action = if (_action != null) _action else Action.Null
  }

  lazy val Null: RuleAspect = apply()

  lazy val isEmpty: RuleAspect => Boolean = ra =>
    ra.variables.isEmpty &&
    ra.conditions.isEmpty &&
    ra.values.isEmpty &&
    ra.pattern.variables.isEmpty && ra.pattern.conditions.isEmpty &&
    ra.action.body.isEmpty

  implicit lazy val encoder: Encoder[RuleAspect] = Encoder.instance { ra =>
    val fields = Seq(
      if (ra.variables.nonEmpty) Some("variables" -> ra.variables.asJson) else None,
      if (ra.conditions.nonEmpty) Some("conditions" -> ra.conditions.asJson) else None,
      if (ra.values.nonEmpty) Some("values" -> ra.values.asJson) else None,
      if (ra.pattern.variables.nonEmpty || ra.pattern.conditions.nonEmpty) Some("pattern" -> ra.pattern.asJson) else None,
      if (ra.action.body.nonEmpty) Some("action" -> ra.action.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }

  implicit lazy val decoder: Decoder[RuleAspect] = Decoder.instance { cursor =>
    for {
      _variables  <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values     <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _pattern    <- cursor.downField("pattern").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
      _action     <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield apply(_variables, _conditions, _values, _pattern, _action)
  }
}
