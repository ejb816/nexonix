package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait DomainAspect extends DracoType {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
}

object DomainAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DomainAspect] = Type[DomainAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[DomainAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.typeName.name.nonEmpty) Some("typeName" -> x.typeName.asJson) else None,
      if (x.elementTypeNames.nonEmpty) Some("elementTypeNames" -> x.elementTypeNames.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[DomainAspect] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _elementTypeNames <- cursor.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield DomainAspect (_typeName, _elementTypeNames)
  }

  def apply (
    _typeName: TypeName = TypeName.Null,
    _elementTypeNames: Seq[String] = Seq.empty
  ) : DomainAspect = new DomainAspect {
    override lazy val typeName: TypeName = _typeName
    override lazy val elementTypeNames: Seq[String] = _elementTypeNames
    override lazy val typeDefinition: TypeDefinition = DomainAspect.typeDefinition
  }

  lazy val Null: DomainAspect = apply()

  lazy val isEmpty: DomainAspect => Boolean = da => da.typeName.name.isEmpty && da.elementTypeNames.isEmpty
}
