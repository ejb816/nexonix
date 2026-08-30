package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait DomainAspect extends DracoType {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
  val source: TypeName
  val target: TypeName
}

object DomainAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DomainAspect] = Type[DomainAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[DomainAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.typeName.name.nonEmpty) Some("typeName" -> x.typeName.asJson) else None,
      if (x.elementTypeNames.nonEmpty) Some("elementTypeNames" -> x.elementTypeNames.asJson) else None,
      if (x.source.name.nonEmpty) Some("source" -> x.source.asJson) else None,
      if (x.target.name.nonEmpty) Some("target" -> x.target.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[DomainAspect] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _elementTypeNames <- cursor.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _source <- cursor.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target <- cursor.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield DomainAspect (_typeName, _elementTypeNames, _source, _target)
  }

  def apply (
    _typeName: TypeName = TypeName.Null,
    _elementTypeNames: Seq[String] = Seq.empty,
    _source: TypeName = TypeName.Null,
    _target: TypeName = TypeName.Null
  ) : DomainAspect = new DomainAspect {
    override lazy val typeName: TypeName = _typeName
    override lazy val elementTypeNames: Seq[String] = _elementTypeNames
    override lazy val source: TypeName = _source
    override lazy val target: TypeName = _target
    override lazy val typeDefinition: TypeDefinition = DomainAspect.typeDefinition
  }

  lazy val Null: DomainAspect = apply()

  lazy val isEmpty: DomainAspect => Boolean = da => da.typeName.name.isEmpty && da.elementTypeNames.isEmpty && da.source.name.isEmpty && da.target.name.isEmpty
}
