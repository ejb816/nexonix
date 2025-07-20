package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait TypePackage {
  val packageName: String
  val namePackage: Seq[String]
}

object TypePackage extends App {
  def apply (
      _packageName: String = "",
      _namePackage: Seq[String] = Seq()
    ) : TypePackage = new TypePackage {
    override val packageName: String = _packageName
    override val namePackage: Seq[String] = if (_packageName.isEmpty) _namePackage
    else _namePackage ++ Seq(_packageName.toLowerCase())
  }

  implicit val encoder: Encoder[TypePackage] = Encoder.instance { t => Json.obj (
      "packageName"  -> Json.fromString(t.packageName),
      "namePackage"  -> t.namePackage.asJson
    )
  }

  implicit val decoder: Decoder[TypePackage] = Decoder.instance { cursor =>
    for {
      _packageName <- cursor.downField("packageName").as[String]
      _namePackage <- cursor.downField("namePackage").as[Seq[String]]
    } yield TypePackage (
      _packageName,
      _namePackage
    )
  }

  lazy val Null: TypePackage = TypePackage()
  println(s"""Declared and compiled type ${
    val name: String = this.getClass.getName
    name.substring(0, name.length - 1)}""")
}