package draco

import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._

trait DomainName extends TypeInstance {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
}

object DomainName extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DomainName",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("elementTypeNames", "Seq[String]")
    ),
    _factory = Factory (
      "DomainName",
      _parameters = Seq (
        Parameter ("typeName", "TypeName", ""),
        Parameter ("elementTypeNames", "Seq[String]", "")
      )
    )
  )
  lazy val typeInstance: Type[DomainName] = Type[DomainName] (typeDefinition)

  def apply (
              _typeName: TypeName,
              _elementTypeNames: Seq[String]
            ) : DomainName = new DomainName {
    override val typeName: TypeName = _typeName
    override val elementTypeNames: Seq[String] = _elementTypeNames
    override val typeInstance: DracoType = DomainName.typeInstance
    override val typeDefinition: TypeDefinition = DomainName.typeDefinition
  }

  lazy val Null: DomainName = DomainName (TypeName.Null, Seq.empty)

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
    } yield DomainName (_typeName, _elementTypeNames)
  }
}
