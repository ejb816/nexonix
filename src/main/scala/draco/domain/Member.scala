package draco.domain

import io.circe.{Decoder, DecodingFailure, Encoder, Json}

sealed trait Member {
  val aName: String
  val aType: String
  val aValue: String
}

object Member {
  implicit val encoder: Encoder[Member] = Encoder.instance { m =>
    val baseFields = Json.obj(
      "name"  -> Json.fromString(m.aName),
      "type"  -> Json.fromString(m.aType),
      "value" -> Json.fromString(m.aValue)
    )
    // Add "kind" to differentiate subtypes:
    m match {
      case fm: Fixed    => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Fixed")))
      case mm: Mutable  => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Mutable")))
      case dm: Dynamic  => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Dynamic")))
      case pm: Parameter => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Parameter")))
    }
  }
  implicit val decoder: Decoder[Member] = Decoder.instance { cursor =>
    // We read "kind" and then decide which subtype to decode
    cursor.downField("kind").as[String].flatMap {
      case "Fixed" =>
        for {
          _name  <- cursor.downField("name").as[String]
          _type   <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Fixed.define(_name, _type, _value)

      case "Mutable" =>
        for {
          _name <- cursor.downField("name").as[String]
          _type <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Mutable.define(_name, _type, _value)

      case "Dynamic" =>
        for {
          _name <- cursor.downField("name").as[String]
          _type <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Dynamic.define(_name, _type, _value)

      case "Parameter" =>
        for {
          _name <- cursor.downField("name").as[String]
          _type <- cursor.downField("type").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Parameter.define(_name, _type, _value)

      case other => Left(DecodingFailure(s"Unknown Member kind: $other", cursor.history))
    }
  }


}

sealed trait Fixed extends Member
object Fixed {
  def define(
              _aName: String,
              _aType: String,
              _aValue: String
            ) : Fixed = {
    new Fixed {
      override val aName: String = _aName
      override val aType: String = _aType
      override val aValue: String = _aValue
    }
  }
}

sealed trait Mutable extends Member
object Mutable {
  def define(
              _aName: String,
              _aType: String,
              _aValue: String
            ) : Mutable = {
    new Mutable {
      override val aName: String = _aName
      override val aType: String = _aType
      override val aValue: String = _aValue
    }
  }
}


sealed trait Dynamic extends Member
object Dynamic {
  def define(
              _aName: String,
              _aType: String,
              _aValue: String
            ) : Dynamic = {
    new Dynamic {
      override val aName: String = _aName
      override val aType: String = _aType
      override val aValue: String = _aValue
    }
  }
}

sealed trait Parameter extends Member
object Parameter {
  def define(
              _aName: String,
              _aType: String,
              _aValue: String
            ) : Parameter = {
    new Parameter {
      override val aName: String = _aName
      override val aType: String = _aType
      override val aValue: String = _aValue
    }
  }
  implicit val encoder: Encoder[Parameter] = Member.encoder.asInstanceOf[Encoder[Parameter]]
  implicit val decoder: Decoder[Parameter] = Member.decoder.asInstanceOf[Decoder[Parameter]]
}
