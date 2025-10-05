package draco

import io.circe.{Decoder, DecodingFailure, Encoder, Json}

sealed trait TypeElement {
  val aName: String
  val aType: String
  val aValue: String
}

object TypeElement extends App {
  def apply (
              _name: String,
              _type: String,
              _value: String = ""
            ) : TypeElement = new TypeElement {
    override val aName: String = _name
    override val aType: String = _type
    override val aValue: String = _value
  }

  lazy implicit val encoder: Encoder[TypeElement] = Encoder.instance { m =>
    val baseFields = Json.obj(
      "name"  -> Json.fromString(m.aName),
      "type"  -> Json.fromString(m.aType),
      "value" -> Json.fromString(m.aValue)
    )
    // Add "kind" to differentiate subtypes:
    m match {
      case _: Fixed    => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Fixed")))
      case _: Mutable  => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Mutable")))
      case _: Dynamic  => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Dynamic")))
      case _: Parameter => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Parameter")))
    }
  }
  lazy implicit val decoder: Decoder[TypeElement] = Decoder.instance { cursor =>
    // We read "kind" and then decide which subtype to decode
    cursor.downField("kind").as[String].flatMap {
      case "Fixed" =>
        for {
          _name  <- cursor.downField("name").as[String]
          _type  <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Fixed (_name, _type, _value)

      case "Mutable" =>
        for {
          _name  <- cursor.downField("name").as[String]
          _type  <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Mutable (_name, _type, _value)

      case "Dynamic" =>
        for {
          _name  <- cursor.downField("name").as[String]
          _type  <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Dynamic (_name, _type, _value)

      case "Parameter" =>
        for {
          _name  <- cursor.downField("name").as[String]
          _type  <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Parameter (_name, _type, _value)

      case other => Left(DecodingFailure(s"Unknown Member kind: $other", cursor.history))
    }
  }
}

sealed trait Fixed extends TypeElement
object Fixed {
  def apply (
              _name: String,
              _type: String,
              _value: String = ""
            ) : Fixed = {
    new Fixed {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
}

sealed trait Mutable extends TypeElement
object Mutable {
  def apply (
              _name: String,
              _type: String,
              _value: String = ""
            ) : Mutable = {
    new Mutable {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
}

sealed trait Dynamic extends TypeElement
object Dynamic {
  def apply (
              _name: String,
              _type: String,
              _value: String = ""
            ) : Dynamic = {
    new Dynamic {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
}

sealed trait Parameter extends TypeElement
object Parameter {
  def apply (
              _name: String,
              _type: String,
              _value: String = ""
            ) : Parameter = {
    new Parameter {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
}
