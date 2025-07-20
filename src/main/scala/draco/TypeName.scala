package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait TypeName {
  val name: String
  val typePackage: TypePackage
  val fullName: String
  val resourcePath: String
}

object TypeName extends App {
  def apply (
              _name: String,
              _typePackage: TypePackage,
            ) : TypeName = {
    new TypeName {
      override val name: String = _name
      override val typePackage: TypePackage = _typePackage
      override val fullName: String = s"${_typePackage.namePackage.mkString(".")}.$name"
      override val resourcePath: String = s"/${_typePackage.namePackage.mkString("/")}.$name.json"
    }
  }

  implicit val encoder: Encoder[TypeName] = Encoder.instance { t => Json.obj(
      "name"         -> Json.fromString(t.name),
      "typePackage"  -> t.typePackage.asJson
    )
  }

  implicit val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[String]
      _typePackage <- cursor.downField("namePackage").as[TypePackage]
    } yield TypeName (
      _name,
      _typePackage
    )
  }

  lazy val Null: TypeName = TypeName (
    _name = "",
    _typePackage = TypePackage.Null
  )
}
