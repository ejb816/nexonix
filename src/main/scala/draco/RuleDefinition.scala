package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}
import draco.TypeName.decoder

sealed trait RuleDefinition {
  val name: String
  val imports: Seq[TypeName]
  val variables: Map[String, String]
  val conditions: Seq[String]
  val values: Map[String, Seq[String]]
  val action: Seq[String]
}

object RuleDefinition {
  def apply (
              _name: String = "DefaultRule",
              _imports: Seq[TypeName] = Seq (),
              _variables: Map[String, String] = Map[String, String](("aString", "String")),
              _conditions: Seq[String] = Seq (),
              _values: Map[String, Seq[String]] = Map[String, Seq[String]](),
              _action: Seq[String] = Seq(
                "ctx.delete(aString)",
                "println(aString)"
              )
            ) : RuleDefinition = {
    new RuleDefinition {
      override val name: String = _name
      override val imports: Seq[TypeName] = _imports
      override val variables: Map[String, String] = _variables
      override val conditions: Seq[String] = _conditions
      override val values: Map[String, Seq[String]] = _values
      override val action: Seq[String] = _action
    }
  }

  // Encode a RuleDefinition.json
  implicit val encoder: Encoder[RuleDefinition] = Encoder.instance { r =>
    Json.obj(
      "name"       -> Json.fromString(r.name),
      "imports"    -> r.imports.asJson,
      "variables"  -> r.variables.asJson,  // Circe provides a default Encoder for Map[String, String]
      "conditions" -> r.conditions.asJson, // and for Seq[String], etc.
      "values"     -> r.values.asJson,     // Map[String, Seq[String]]
      "action"     -> r.action.asJson
    )
  }

  implicit val decoder: Decoder[RuleDefinition] = Decoder.instance { cursor =>
    for {
      _name       <- cursor.downField("name").as[String]
      _imports    <- cursor.downField("imports").as[Option[Seq[TypeName]]]
      _variables  <- cursor.downField("variables").as[Map[String, String]]
      _conditions <- cursor.downField("conditions").as[Seq[String]]
      _values     <- cursor.downField("values").as[Map[String, Seq[String]]]
      _action     <- cursor.downField("action").as[Seq[String]]
    } yield RuleDefinition (
      _name,
      _imports.getOrElse(Seq ()),
      _variables,
      _conditions,
      _values,
      _action
    )
  }
}
