package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait CodecAspect extends DracoType {
  val discriminator: String
  val payload: Seq[String]
}

object CodecAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CodecAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[CodecAspect] = Type[CodecAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[CodecAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.discriminator.nonEmpty) Some("discriminator" -> x.discriminator.asJson) else None,
      if (x.payload.nonEmpty) Some("payload" -> x.payload.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[CodecAspect] = Decoder.instance { cursor =>
    for {
      _discriminator <- cursor.downField("discriminator").as[Option[String]].map(_.getOrElse(""))
      _payload <- cursor.downField("payload").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield CodecAspect (_discriminator, _payload)
  }

  def apply (
    _discriminator: => String = "",
    _payload: => Seq[String] = Seq.empty
  ) : CodecAspect = new CodecAspect {
    override lazy val discriminator: String = _discriminator
    override lazy val payload: Seq[String] = _payload
    override lazy val typeDefinition: TypeDefinition = CodecAspect.typeDefinition
  }

  lazy val Null: CodecAspect = apply()

  lazy val isEmpty: CodecAspect => Boolean = ca => ca.discriminator.isEmpty && ca.payload.isEmpty
}
