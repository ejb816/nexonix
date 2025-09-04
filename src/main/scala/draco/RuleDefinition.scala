package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait RuleDefinition {
  val name: String
  val variables: Map[String, String]
  val conditions: Seq[String]
  val values: Map[String, Seq[String]]
  val action: Seq[String]
}

object RuleDefinition {
  def apply (
              _name: String = "Default Rule",
              _variables: Map[String, String] = Map[String, String](),
              _conditions: Seq[String] = Seq(),
              _values: Map[String, Seq[String]] = Map[String, Seq[String]](),
              _action: Seq[String] = Seq("println(\"Default Rule\")")
            ) : RuleDefinition = {
    new RuleDefinition {
      override val name: String = _name
      override val variables: Map[String, String] = _variables
      override val conditions: Seq[String] = _conditions
      override val values: Map[String, Seq[String]] = _values
      override val action: Seq[String] = _action
    }
  }

  // Encode a RuleDefinition
  implicit val encoder: Encoder[RuleDefinition] = Encoder.instance { r =>
    Json.obj(
      "name"       -> Json.fromString(r.name),
      "variables"  -> r.variables.asJson,  // Circe provides a default Encoder for Map[String, String]
      "conditions" -> r.conditions.asJson, // and for Seq[String], etc.
      "values"     -> r.values.asJson,     // Map[String, Seq[String]]
      "action"     -> r.action.asJson
    )
  }

  implicit val decoder: Decoder[RuleDefinition] = Decoder.instance { cursor =>
    for {
      _name       <- cursor.downField("name").as[String]
      _variables  <- cursor.downField("variables").as[Map[String, String]]
      _conditions <- cursor.downField("conditions").as[Seq[String]]
      _values     <- cursor.downField("values").as[Map[String, Seq[String]]]
      _action     <- cursor.downField("action").as[Seq[String]]
    } yield RuleDefinition (
      _name,
      _variables,
      _conditions,
      _values,
      _action
    )
  }
}
