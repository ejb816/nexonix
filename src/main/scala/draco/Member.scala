package draco

import io.circe.{Decoder, DecodingFailure, Encoder, Json}

sealed trait Member extends Draco{
  val aName: String
  val aType: String
  val aValue: String
}

object Member extends App {
  def apply (
              _name: String,
              _type: String,
              _value: String
            ) : Member = new Member {
    override val aName: String = _name
    override val aType: String = _type
    override val aValue: String = _value
  }

  implicit val encoder: Encoder[Member] = Encoder.instance { m =>
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
  implicit val decoder: Decoder[Member] = Decoder.instance { cursor =>
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

sealed trait Fixed extends Member
object Fixed extends App {
  def apply (
              _name: String,
              _type: String,
              _value: String
            ) : Fixed = {
    new Fixed {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
  println(s"""Declared and compiled type ${
    val name: String = this.getClass.getName
    name.substring(0, name.length - 1)}""")
}

sealed trait Mutable extends Member
object Mutable extends App {
  def apply (
              _name: String,
              _type: String,
              _value: String
            ) : Mutable = {
    new Mutable {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
  println(s"""Declared and compiled type ${
    val name: String = this.getClass.getName
    name.substring(0, name.length - 1)}""")
}


sealed trait Dynamic extends Member
object Dynamic extends App {
  def apply (
              _name: String,
              _type: String,
              _value: String
            ) : Dynamic = {
    new Dynamic {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
  println(s"""Declared and compiled type ${
    val name: String = this.getClass.getName
    name.substring(0, name.length - 1)}""")
}

sealed trait Parameter extends Member
object Parameter extends App {
  def apply (
              _name: String,
              _type: String,
              _value: String
            ) : Parameter = {
    new Parameter {
      override val aName: String = _name
      override val aType: String = _type
      override val aValue: String = _value
    }
  }
  implicit val encoder: Encoder[Parameter] = Member.encoder.asInstanceOf[Encoder[Parameter]]
  implicit val decoder: Decoder[Parameter] = Member.decoder.asInstanceOf[Decoder[Parameter]]
  println(s"""Declared and compiled type ${
    val name: String = this.getClass.getName
    name.substring(0, name.length - 1)}""")
}

object CheckMember extends App {
  Member.main(Array())
  Fixed.main(Array())
  Mutable.main(Array())
  Dynamic.main(Array())
  Parameter.main(Array())
}

