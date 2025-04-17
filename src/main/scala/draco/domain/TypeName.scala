package draco.domain

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeName {
  val name: String
  val domain: String
  val namePackage: Seq[String]
  val fullName: String
  val resourcePath: String
}

object TypeName {
  def apply (
              _name: String,
              _domain: String = "Domain",
              _namePackage: Seq[String] = Seq("draco")
            ) : TypeName = {
    new TypeName {
      override val name: String = _name
      override val domain: String = _domain
      override val namePackage: Seq[String] = _namePackage ++ Seq(_domain.toLowerCase())
      override val fullName: String = s"${namePackage.mkString(".")}.$name"
      override val resourcePath: String = s"/${namePackage.mkString("/")}.${name}.json"
    }
  }

  implicit val encoder: Encoder[TypeName] = Encoder.instance { t =>
    Json.obj(
      "name"         -> Json.fromString(t.name),
      "domain"       -> Json.fromString(t.domain),
      "namePackage"  -> t.namePackage.asJson
    )
  }

  implicit val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      _name         <- cursor.downField("name").as[String]
      _domain       <- cursor.downField("name").as[String]
      _namePackage  <- cursor.downField("namePackage").as[Seq[String]]
    } yield TypeName (
      _name,
      _domain,
      _namePackage
    )
  }
}
