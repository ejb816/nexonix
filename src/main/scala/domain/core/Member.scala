package domain.core

import io.circe.{Decoder, DecodingFailure, Encoder, Json}

sealed trait Member {
  val memberName: String
  val memberType: String
  val memberValue: String
}

object Member {
  implicit val encode: Encoder[Member] = Encoder.instance { m =>
    val baseFields = Json.obj(
      "memberName"  -> Json.fromString(m.memberName),
      "memberType"  -> Json.fromString(m.memberType),
      "memberValue" -> Json.fromString(m.memberValue)
    )
    // Add "kind" to differentiate subtypes:
    m match {
      case fm: Fixed    => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Fixed")))
      case mm: Mutable  => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Mutable")))
      case dm: Dynamic  => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Dynamic")))
      case pm: Parameter => baseFields.deepMerge(Json.obj("kind" -> Json.fromString("Parameter")))
    }
  }
  implicit val decode: Decoder[Member] = Decoder.instance { cursor =>
    // We read "kind" and then decide which subtype to decode
    cursor.downField("kind").as[String].flatMap {
      case "Fixed" =>
        for {
          name  <- cursor.downField("memberName").as[String]
          tpe   <- cursor.downField("memberType").as[String]
          value <- cursor.downField("memberValue").as[String]
        } yield Fixed.define(name, tpe, value)

      case "Mutable" =>
        for {
          name  <- cursor.downField("memberName").as[String]
          tpe   <- cursor.downField("memberType").as[String]
          value <- cursor.downField("memberValue").as[String]
        } yield Mutable.define(name, tpe, value)

      case "Dynamic" =>
        for {
          name  <- cursor.downField("memberName").as[String]
          tpe   <- cursor.downField("memberType").as[String]
          value <- cursor.downField("memberValue").as[String]
        } yield Dynamic.define(name, tpe, value)

      case "Parameter" =>
        for {
          name  <- cursor.downField("memberName").as[String]
          tpe   <- cursor.downField("memberType").as[String]
          value <- cursor.downField("memberValue").as[String]
        } yield Parameter.define(name, tpe, value)

      case other => Left(DecodingFailure(s"Unknown Member kind: $other", cursor.history))
    }
  }


}

sealed trait Fixed extends Member
object Fixed {
  def define(
              _memberName: String,
              _memberType: String,
              _memberValue: String
            ) : Fixed = {
    new Fixed {
      override val memberName: String = _memberName
      override val memberType: String = _memberType
      override val memberValue: String = _memberValue
    }
  }
}

sealed trait Mutable extends Member
object Mutable {
  def define(
              _memberName: String,
              _memberType: String,
              _memberValue: String
            ) : Mutable = {
    new Mutable {
      override val memberName: String = _memberName
      override val memberType: String = _memberType
      override val memberValue: String = _memberValue
    }
  }
}


sealed trait Dynamic extends Member
object Dynamic {
  def define(
              _memberName: String,
              _memberType: String,
              _memberValue: String
            ) : Dynamic = {
    new Dynamic {
      override val memberName: String = _memberName
      override val memberType: String = _memberType
      override val memberValue: String = _memberValue
    }
  }
}

sealed trait Parameter extends Member
object Parameter {
  def define(
              _memberName: String,
              _memberType: String,
              _memberValue: String
            ) : Parameter = {
    new Parameter {
      override val memberName: String = _memberName
      override val memberType: String = _memberType
      override val memberValue: String = _memberValue
    }
  }
  implicit val encode: Encoder[Parameter] = Member.encode.asInstanceOf[Encoder[Parameter]]
  implicit val decode: Decoder[Parameter] = Member.decode.asInstanceOf[Decoder[Parameter]]
}
