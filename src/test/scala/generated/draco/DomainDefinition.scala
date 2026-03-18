
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait DomainDefinition extends TypeInstance {
  val typeName: TypeName
  val elementTypeNames: Seq[String]
  val superDomain: TypeName
  val source: TypeName
  val target: TypeName
}

object DomainDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("DomainDefinition", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[DomainDefinition] = Type[DomainDefinition] (typeDefinition)

  implicit lazy val encoder: Encoder[DomainDefinition] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeName" -> x.typeName.asJson),
      if (x.elementTypeNames.nonEmpty) Some("elementTypeNames" -> x.elementTypeNames.asJson) else None,
      if (x.superDomain.name.nonEmpty) Some("superDomain" -> x.superDomain.asJson) else None,
      if (x.source.name.nonEmpty) Some("source" -> x.source.asJson) else None,
      if (x.target.name.nonEmpty) Some("target" -> x.target.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[DomainDefinition] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[TypeName]
      _elementTypeNames <- cursor.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _superDomain <- cursor.downField("superDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _source <- cursor.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target <- cursor.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield DomainDefinition (_typeName, _elementTypeNames, _superDomain, _source, _target)
  }

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
    override lazy val typeInstance: DracoType = DomainDefinition.typeInstance
    override lazy val typeDefinition: TypeDefinition = DomainDefinition.typeDefinition
  }

  lazy val Null: DomainDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _elementTypeNames = Seq.empty,
    _superDomain = TypeName.Null,
    _source = TypeName.Null,
    _target = TypeName.Null
  )


}
