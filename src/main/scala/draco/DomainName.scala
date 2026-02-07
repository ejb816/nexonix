package draco

import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._

trait DomainName {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
}

object DomainName {
  val Null: DomainName = new DomainName {
    override val typeName: TypeName = TypeName.Null
    override val elementTypeNames: Seq[String] = Seq ()
  }
  def apply (_typeName: TypeName, _elementTypeNames: Seq[String]) : DomainName = new DomainName {
    override val typeName: TypeName = _typeName
    override val elementTypeNames: Seq[String] = _elementTypeNames
  }

  lazy implicit val encoder: Encoder[DomainName] = Encoder.instance { m =>
    val fields = Seq(
      Some("typeName" -> m.typeName.asJson),
      if (m.elementTypeNames.nonEmpty) Some("elementTypeNames" -> m.elementTypeNames.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  lazy implicit val decoder: Decoder[DomainName] = Decoder.instance { c =>
    for {
      _typeName         <- c.downField("typeName").as[TypeName]
      _elementTypeNames <- c.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield new DomainName {
      override val typeName: TypeName            = _typeName
      override val elementTypeNames: Seq[String] = _elementTypeNames
    }
  }
}
