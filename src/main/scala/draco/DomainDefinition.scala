package draco

import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._

sealed trait DomainDefinition extends TypeInstance {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
  val superDomain: TypeName
  val source: TypeName
  val target: TypeName
}

object DomainDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DomainDefinition",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("elementTypeNames", "Seq[String]"),
      Fixed ("superDomain", "TypeName"),
      Fixed ("source", "TypeName"),
      Fixed ("target", "TypeName")
    ),
    _factory = Factory (
      "DomainDefinition",
      _parameters = Seq (
        Parameter ("typeName", "TypeName", ""),
        Parameter ("elementTypeNames", "Seq[String]", "Seq.empty"),
        Parameter ("superDomain", "TypeName", "TypeName.Null"),
        Parameter ("source", "TypeName", "TypeName.Null"),
        Parameter ("target", "TypeName", "TypeName.Null")
      )
    )
  )
  lazy val typeInstance: Type[DomainDefinition] = Type[DomainDefinition] (typeDefinition)

  def apply (
              _typeName: TypeName,
              _elementTypeNames: Seq[String] = Seq.empty,
              _superDomain: TypeName = TypeName.Null,
              _source: TypeName = TypeName.Null,
              _target: TypeName = TypeName.Null
            ) : DomainDefinition = new DomainDefinition {
    override val typeName: TypeName = _typeName
    override val elementTypeNames: Seq[String] = _elementTypeNames
    override val superDomain: TypeName = _superDomain
    override val source: TypeName = _source
    override val target: TypeName = _target
    override val typeInstance: DracoType = DomainDefinition.typeInstance
    override val typeDefinition: TypeDefinition = DomainDefinition.typeDefinition
  }

  lazy val Null: DomainDefinition = DomainDefinition (TypeName.Null)

  lazy implicit val encoder: Encoder[DomainDefinition] = Encoder.instance { m =>
    val fields = Seq(
      Some("typeName" -> m.typeName.asJson),
      if (m.elementTypeNames.nonEmpty) Some("elementTypeNames" -> m.elementTypeNames.asJson) else None,
      if (m.superDomain.name.nonEmpty) Some("superDomain" -> m.superDomain.asJson) else None,
      if (m.source.name.nonEmpty) Some("source" -> m.source.asJson) else None,
      if (m.target.name.nonEmpty) Some("target" -> m.target.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  lazy implicit val decoder: Decoder[DomainDefinition] = Decoder.instance { c =>
    for {
      _typeName         <- c.downField("typeName").as[TypeName]
      _elementTypeNames <- c.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _superDomain      <- c.downField("superDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _source           <- c.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target           <- c.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield DomainDefinition (_typeName, _elementTypeNames, _superDomain, _source, _target)
  }
}
