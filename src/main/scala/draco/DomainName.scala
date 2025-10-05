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
  def apply (_typeName: TypeName, _elementNames: Seq[String] = Seq ()) : DomainName = new DomainName {
    override val typeName: TypeName = _typeName
    override val elementTypeNames: Seq[String] = _elementNames
  }

  lazy implicit val encoder: Encoder[DomainName] = Encoder.instance { m =>
    Json.obj(
      "typeName"     -> m.typeName.asJson,
      "elementNames" -> m.elementTypeNames.asJson
    )
  }

  lazy implicit val decoder: Decoder[DomainName] = Decoder.instance { c =>
    for {
      _typeName     <- c.downField("typeName").as[TypeName]
      _elementNames <- c.downField("elementNames").as[Seq[String]]
    } yield new DomainName {
      override val typeName: TypeName        = _typeName
      override val elementTypeNames: Seq[String] = _elementNames
    }
  }
}
