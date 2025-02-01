package domain.core

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeName {
  val name: String
  val namePackage: Seq[String]
}

object TypeName {
  def define(
              _name: String,
              _namePackage: Seq[String]
            ) : TypeName = {
    new TypeName {
      override val name: String = _name
      override val namePackage: Seq[String] = _namePackage
    }
  }
  // 1) Encode a TypeName
  implicit val encode: Encoder[TypeName] = Encoder.instance { t =>
    Json.obj(
      "name"         -> Json.fromString(t.name),
      "namePackage"  -> t.namePackage.asJson,
    )
  }

  implicit val decodeTypeName: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      name         <- cursor.downField("name").as[String]
      namePackage  <- cursor.downField("namePackage").as[Seq[String]]
    } yield TypeName.define(name, namePackage)
  }
}
