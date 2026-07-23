package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait DracoAspect extends DracoType {
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

object DracoAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DracoAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DracoAspect] = Type[DracoAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[DracoAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.superDomain.name.nonEmpty) Some("superDomain" -> x.superDomain.asJson) else None,
      if (x.modules.nonEmpty) Some("modules" -> x.modules.asJson) else None,
      if (x.extensible.name.nonEmpty) Some("extensible" -> x.extensible.asJson) else None,
      if (x.derivation.nonEmpty) Some("derivation" -> x.derivation.asJson) else None,
      if (x.elements.nonEmpty) Some("elements" -> x.elements.asJson) else None,
      if (x.factory.valueType.nonEmpty) Some("factory" -> x.factory.asJson) else None,
      if (x.globalElements.nonEmpty) Some("globalElements" -> x.globalElements.asJson) else None,
      if (x.source.name.nonEmpty) Some("source" -> x.source.asJson) else None,
      if (x.target.name.nonEmpty) Some("target" -> x.target.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[DracoAspect] = Decoder.instance { cursor =>
    for {
      _superDomain <- cursor.downField("superDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _modules <- cursor.downField("modules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _extensible <- cursor.downField("extensible").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _derivation <- cursor.downField("derivation").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _elements <- cursor.downField("elements").as[Option[Seq[TypeElement]]].map(_.getOrElse(Seq.empty))
      _factory <- cursor.downField("factory").as[Option[Factory]].map(_.getOrElse(Factory.Null))
      _globalElements <- cursor.downField("globalElements").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
      _source <- cursor.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target <- cursor.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield DracoAspect (_superDomain, _modules, _extensible, _derivation, _elements, _factory, _globalElements, _source, _target)
  }

  def apply (
    _superDomain: TypeName = TypeName.Null,
    _modules: Seq[TypeName] = Seq.empty,
    _extensible: TypeName = TypeName.Null,
    _derivation: Seq[TypeName] = Seq.empty,
    _elements: Seq[TypeElement] = Seq.empty,
    _factory: Factory = Factory.Null,
    _globalElements: Seq[BodyElement] = Seq.empty,
    _source: TypeName = TypeName.Null,
    _target: TypeName = TypeName.Null
  ) : DracoAspect = new DracoAspect {
    override lazy val superDomain: TypeName = _superDomain
    override lazy val modules: Seq[TypeName] = _modules
    override lazy val extensible: TypeName = _extensible
    override lazy val derivation: Seq[TypeName] = _derivation
    override lazy val elements: Seq[TypeElement] = _elements
    override lazy val factory: Factory = _factory
    override lazy val globalElements: Seq[BodyElement] = _globalElements
    override lazy val source: TypeName = _source
    override lazy val target: TypeName = _target
    override lazy val typeDefinition: TypeDefinition = DracoAspect.typeDefinition
  }

  lazy val Null: DracoAspect = apply()

  lazy val isEmpty: DracoAspect => Boolean = da => da.superDomain.name.isEmpty && da.modules.isEmpty && da.extensible.name.isEmpty && da.derivation.isEmpty && da.elements.isEmpty && da.factory.valueType.isEmpty && da.globalElements.isEmpty && da.source.name.isEmpty && da.target.name.isEmpty
}
