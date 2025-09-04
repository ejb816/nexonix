package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait TypePackage extends Draco {
  val packageName: String
  val namePackage: Seq[String]
}

object TypePackage extends App {
  def apply (
      _packageName: String,
      _namePackage: Seq[String]
    ) : TypePackage = new TypePackage {
    override val packageName: String = _packageName
    override val namePackage: Seq[String] = _namePackage
  }

  lazy implicit val encoder: Encoder[TypePackage] = Encoder.instance { t => Json.obj (
      "packageName"  -> Json.fromString(t.packageName),
      "namePackage"  -> t.namePackage.asJson
    )
  }

  lazy implicit val decoder: Decoder[TypePackage] = Decoder.instance { cursor =>
    for {
      _packageName <- cursor.downField("packageName").as[String]
      _namePackage <- cursor.downField("namePackage").as[Seq[String]]
    } yield TypePackage (
      _packageName,
      _namePackage
    )
  }

  lazy val Null: TypePackage = TypePackage("", Seq[String]())
}