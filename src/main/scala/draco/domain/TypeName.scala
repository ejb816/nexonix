package draco.domain

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeName {
  val name: String
  val namePackage: Seq[String]
  val fullName: String
}

object TypeName {
  def apply (
              _name: String,
              _namePackage: Seq[String]
            ) : TypeName = {
    new TypeName {
      override val name: String = _name
      override val namePackage: Seq[String] = _namePackage
      override val fullName: String = s"${namePackage.mkString(".")}.$name"
    }
  }

  implicit val encoder: Encoder[TypeName] = Encoder.instance { t =>
    Json.obj(
      "name"         -> Json.fromString(t.name),
      "namePackage"  -> t.namePackage.asJson,
    )
  }

  implicit val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      name         <- cursor.downField("name").as[String]
      namePackage  <- cursor.downField("namePackage").as[Seq[String]]
    } yield TypeName (name, namePackage)
  }
}
