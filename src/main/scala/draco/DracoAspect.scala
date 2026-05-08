package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait DracoAspect extends Extensible {
  val superDomain: TypeName
  val modules: Seq[TypeName]
  val extensible: TypeName
  val derivation: Seq[TypeName]
  val elements: Seq[TypeElement]
  val factory: Factory
  val globalElements: Seq[BodyElement]
  val source: TypeName
  val target: TypeName
}

object DracoAspect {
  def apply(
             _superDomain: TypeName = TypeName.Null,
             _modules: Seq[TypeName] = Seq.empty,
             _extensible: TypeName = TypeName.Null,
             _derivation: Seq[TypeName] = Seq.empty,
             _elements: Seq[TypeElement] = Seq.empty,
             _factory: Factory = Factory.Null,
             _globalElements: Seq[BodyElement] = Seq.empty,
             _source: TypeName = TypeName.Null,
             _target: TypeName = TypeName.Null
           ): DracoAspect = new DracoAspect {
    override val superDomain: TypeName = _superDomain
    override val modules: Seq[TypeName] = _modules
    override val extensible: TypeName = _extensible
    override val derivation: Seq[TypeName] = _derivation
    override val elements: Seq[TypeElement] = _elements
    override val factory: Factory = _factory
    override val globalElements: Seq[BodyElement] = _globalElements
    override val source: TypeName = _source
    override val target: TypeName = _target
  }

  lazy val Null: DracoAspect = apply()

  lazy val isEmpty: DracoAspect => Boolean = da =>
    da.superDomain.name.isEmpty &&
    da.modules.isEmpty &&
    da.extensible.name.isEmpty &&
    da.derivation.isEmpty &&
    da.elements.isEmpty &&
    da.factory.valueType.isEmpty &&
    da.globalElements.isEmpty &&
    da.source.name.isEmpty &&
    da.target.name.isEmpty

  implicit lazy val encoder: Encoder[DracoAspect] = Encoder.instance { da =>
    val fields = Seq(
      if (da.superDomain.name.nonEmpty) Some("superDomain" -> da.superDomain.asJson) else None,
      if (da.modules.nonEmpty) Some("modules" -> da.modules.asJson) else None,
      if (da.extensible.name.nonEmpty) Some("extensible" -> da.extensible.asJson) else None,
      if (da.derivation.nonEmpty) Some("derivation" -> da.derivation.asJson) else None,
      if (da.elements.nonEmpty) Some("elements" -> da.elements.asJson) else None,
      if (da.factory.valueType.nonEmpty) Some("factory" -> da.factory.asJson) else None,
      if (da.globalElements.nonEmpty) Some("globalElements" -> da.globalElements.asJson) else None,
      if (da.source.name.nonEmpty) Some("source" -> da.source.asJson) else None,
      if (da.target.name.nonEmpty) Some("target" -> da.target.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }

  implicit lazy val decoder: Decoder[DracoAspect] = Decoder.instance { cursor =>
    for {
      _superDomain    <- cursor.downField("superDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _modules        <- cursor.downField("modules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _extensible     <- cursor.downField("extensible").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _derivation     <- cursor.downField("derivation").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _elements       <- cursor.downField("elements").as[Option[Seq[TypeElement]]].map(_.getOrElse(Seq.empty))
      _factory        <- cursor.downField("factory").as[Option[Factory]].map(_.getOrElse(Factory.Null))
      _globalElements <- cursor.downField("globalElements").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
      _source         <- cursor.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target         <- cursor.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield apply(
      _superDomain,
      _modules,
      _extensible,
      _derivation,
      _elements,
      _factory,
      _globalElements,
      _source,
      _target
    )
  }
}
