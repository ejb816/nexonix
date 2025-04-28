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
              _domain: String,
              _namePackage: Seq[String]
            ) : TypeName = {
    new TypeName {
      override val name: String = _name
      override val domain: String = _domain
      override val namePackage: Seq[String] = if (_domain.isEmpty)
        _namePackage else _namePackage ++ Seq(_domain.toLowerCase())
      override val fullName: String = s"${namePackage.mkString(".")}.$name"
      override val resourcePath: String = s"/${namePackage.mkString("/")}.${name}.json"
    }
  }

  implicit val encoder: Encoder[TypeName] = Encoder.instance { t => Json.obj(
      "name"         -> Json.fromString(t.name),
      "domain"       -> Json.fromString(t.domain),
      "namePackage"  -> t.namePackage.asJson
    )
  }

  implicit val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    cursor.downField("name").as[String]
      .flatMap(_name =>
        cursor.downField("domain").as[String]
          .flatMap(_domain =>
            cursor.downField("namePackage").as[Seq[String]]
              .map(_namePackage => TypeName(_name, _domain, _namePackage))
          )
      )
  }

  val Null: TypeName = TypeName(
    _name = "",
    _domain = "",
    _namePackage = Seq[String]()
  )
}
