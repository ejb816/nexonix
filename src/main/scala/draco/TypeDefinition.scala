package draco

import draco.drake.Absent
import draco.drake.Presence

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeDefinition extends Aspects {
  val typeName: TypeName
  val uses: Seq[TypeName]
}

object TypeDefinition extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeDefinition", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeDefinition] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeName" -> x.typeName.asJson),
      if (x.uses.nonEmpty) Some("uses" -> x.uses.asJson) else None,
      if (!DracoAspect.isEmpty(x.dracoAspect)) Some("dracoAspect" -> x.dracoAspect.asJson) else None,
      if (!DomainAspect.isEmpty(x.domainAspect)) Some("domainAspect" -> x.domainAspect.asJson) else None,
      if (x.ruleAspect.fold(false, _ => true)) Some("ruleAspect" -> x.ruleAspect.fold[Json](Json.Null, aspect => aspect.asJson)) else None,
      if (x.actorAspect.fold(false, _ => true)) Some("actorAspect" -> x.actorAspect.fold[Json](Json.Null, aspect => aspect.asJson)) else None,
      if (!CodecAspect.isEmpty(x.codecAspect)) Some("codecAspect" -> x.codecAspect.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[TypeName]
      _uses <- cursor.downField("uses").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _dracoAspect <- cursor.downField("dracoAspect").as[Option[DracoAspect]].map(_.getOrElse(DracoAspect.Null))
      _domainAspect <- cursor.downField("domainAspect").as[Option[DomainAspect]].map(_.getOrElse(DomainAspect.Null))
      _ruleAspect <- (if (cursor.keys.exists(_.exists(_ == "ruleAspect"))) cursor.downField("ruleAspect").as[RuleAspect].map(value => draco.drake.Present(value): Presence[RuleAspect]) else Right(Absent[RuleAspect]()))
      _actorAspect <- (if (cursor.keys.exists(_.exists(_ == "actorAspect"))) cursor.downField("actorAspect").as[ActorAspect].map(value => draco.drake.Present(value): Presence[ActorAspect]) else Right(Absent[ActorAspect]()))
      _codecAspect <- cursor.downField("codecAspect").as[Option[CodecAspect]].map(_.getOrElse(CodecAspect.Null))
    } yield TypeDefinition (_typeName, _uses, _dracoAspect, _domainAspect, _ruleAspect, _actorAspect, _codecAspect)
  }

  def apply (
    _typeName: => TypeName,
    _uses: => Seq[TypeName] = Seq.empty,
    _dracoAspect: => DracoAspect = DracoAspect.Null,
    _domainAspect: => DomainAspect = DomainAspect.Null,
    _ruleAspect: => Presence[RuleAspect] = Absent(),
    _actorAspect: => Presence[ActorAspect] = Absent(),
    _codecAspect: => CodecAspect = CodecAspect.Null
  ) : TypeDefinition = new TypeDefinition {
    override lazy val typeName: TypeName = _typeName
    override lazy val uses: Seq[TypeName] = _uses
    override lazy val dracoAspect: DracoAspect = _dracoAspect
    override lazy val domainAspect: DomainAspect = _domainAspect
    override lazy val ruleAspect: Presence[RuleAspect] = _ruleAspect
    override lazy val actorAspect: Presence[ActorAspect] = _actorAspect
    override lazy val codecAspect: CodecAspect = _codecAspect
    override lazy val typeDefinition: TypeDefinition = TypeDefinition.typeDefinition
  }

  lazy val Null: TypeDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _uses = Seq.empty,
    _dracoAspect = DracoAspect.Null,
    _domainAspect = DomainAspect.Null,
    _ruleAspect = Absent(),
    _actorAspect = Absent(),
    _codecAspect = CodecAspect.Null
  )


}
