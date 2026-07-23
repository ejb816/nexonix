package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait CodecAspect extends DracoType {
  val discriminator: String
}

object CodecAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CodecAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[CodecAspect] = Type[CodecAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[CodecAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.discriminator.nonEmpty) Some("discriminator" -> x.discriminator.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[CodecAspect] = Decoder.instance { cursor =>
    for {
      _discriminator <- cursor.downField("discriminator").as[Option[String]].map(_.getOrElse(""))
    } yield CodecAspect (_discriminator)
  }

  def apply (
    _discriminator: String = ""
  ) : CodecAspect = new CodecAspect {
    override lazy val discriminator: String = _discriminator
    override lazy val typeDefinition: TypeDefinition = CodecAspect.typeDefinition
  }

  lazy val Null: CodecAspect = apply()

  lazy val isEmpty: CodecAspect => Boolean = ca => ca.discriminator.isEmpty
}
