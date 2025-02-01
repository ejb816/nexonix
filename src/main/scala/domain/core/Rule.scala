package domain.core

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, DecodingFailure, Encoder, Json}

sealed trait Rule {
  val name: String
  val variables: Map[String, String]
  val conditions: Seq[String]
  val values: Map[String, Seq[String]]
  val action: Seq[String]
}

object Rule {
  def define(
              _name: String,
              _variables: Map[String, String],
              _conditions: Seq[String],
              _values: Map[String, Seq[String]],
              _action: Seq[String]
            ) : Rule = {
    new Rule {
      override val name: String = _name
      override val variables: Map[String, String] = _variables
      override val conditions: Seq[String] = _conditions
      override val values: Map[String, Seq[String]] = _values
      override val action: Seq[String] = _action
    }
  }

  // Encode a RuleDefinition
  implicit val encode: Encoder[Rule] = Encoder.instance { r =>
    Json.obj(
      "name"       -> Json.fromString(r.name),
      "variables"  -> r.variables.asJson,  // Circe provides a default Encoder for Map[String, String]
      "conditions" -> r.conditions.asJson, // and for Seq[String], etc.
      "values"     -> r.values.asJson,     // Map[String, Seq[String]]
      "action"     -> r.action.asJson
    )
  }

  implicit val decodeRuleDefinition: Decoder[Rule] = Decoder.instance { cursor =>
    for {
      _name       <- cursor.downField("name").as[String]
      _variables  <- cursor.downField("variables").as[Map[String, String]]
      _conditions <- cursor.downField("conditions").as[Seq[String]]
      _values     <- cursor.downField("values").as[Map[String, Seq[String]]]
      _action     <- cursor.downField("action").as[Seq[String]]
    } yield Rule.define(
      _name,
      _variables,
      _conditions,
      _values,
      _action
    )
  }
}
