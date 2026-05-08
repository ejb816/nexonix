package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait DomainAspect extends Extensible {
  val elementTypeNames: Seq[String]
}

object DomainAspect {
  def apply(
             _elementTypeNames: Seq[String] = Seq.empty
           ): DomainAspect = new DomainAspect {
    override val elementTypeNames: Seq[String] = _elementTypeNames
  }

  lazy val Null: DomainAspect = apply()

  lazy val isEmpty: DomainAspect => Boolean = da => da.elementTypeNames.isEmpty

  implicit lazy val encoder: Encoder[DomainAspect] = Encoder.instance { da =>
    val fields = Seq(
      if (da.elementTypeNames.nonEmpty) Some("elementTypeNames" -> da.elementTypeNames.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }

  implicit lazy val decoder: Decoder[DomainAspect] = Decoder.instance { cursor =>
    for {
      _elementTypeNames <- cursor.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield apply(_elementTypeNames)
  }
}
