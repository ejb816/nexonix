package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeDefinition extends Aspects {
  val typeName: TypeName
}

object TypeDefinition extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("TypeDefinition", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeDefinition] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeName" -> x.typeName.asJson),
      if (!DracoAspect.isEmpty(x.dracoAspect)) Some("dracoAspect" -> x.dracoAspect.asJson) else None,
      if (!DomainAspect.isEmpty(x.domainAspect)) Some("domainAspect" -> x.domainAspect.asJson) else None,
      if (!RuleAspect.isEmpty(x.ruleAspect)) Some("ruleAspect" -> x.ruleAspect.asJson) else None,
      if (!ActorAspect.isEmpty(x.actorAspect)) Some("actorAspect" -> x.actorAspect.asJson) else None,
      if (!CodecAspect.isEmpty(x.codecAspect)) Some("codecAspect" -> x.codecAspect.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[TypeName]
      _dracoAspect <- cursor.downField("dracoAspect").as[Option[DracoAspect]].map(_.getOrElse(DracoAspect.Null))
      _domainAspect <- cursor.downField("domainAspect").as[Option[DomainAspect]].map(_.getOrElse(DomainAspect.Null))
      _ruleAspect <- cursor.downField("ruleAspect").as[Option[RuleAspect]].map(_.getOrElse(RuleAspect.Null))
      _actorAspect <- cursor.downField("actorAspect").as[Option[ActorAspect]].map(_.getOrElse(ActorAspect.Null))
      _codecAspect <- cursor.downField("codecAspect").as[Option[CodecAspect]].map(_.getOrElse(CodecAspect.Null))
    } yield TypeDefinition (_typeName, _dracoAspect, _domainAspect, _ruleAspect, _actorAspect, _codecAspect)
  }

  def apply (
    _typeName: TypeName,
    _dracoAspect: DracoAspect = DracoAspect.Null,
    _domainAspect: DomainAspect = DomainAspect.Null,
    _ruleAspect: RuleAspect = RuleAspect.Null,
    _actorAspect: ActorAspect = ActorAspect.Null,
    _codecAspect: CodecAspect = CodecAspect.Null
  ) : TypeDefinition = new TypeDefinition {
    override lazy val typeName: TypeName = _typeName
    override lazy val dracoAspect: DracoAspect = _dracoAspect
    override lazy val domainAspect: DomainAspect = _domainAspect
    override lazy val ruleAspect: RuleAspect = _ruleAspect
    override lazy val actorAspect: ActorAspect = _actorAspect
    override lazy val codecAspect: CodecAspect = _codecAspect
    override lazy val typeDefinition: TypeDefinition = TypeDefinition.typeDefinition
  }

  lazy val Null: TypeDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _dracoAspect = DracoAspect.Null,
    _domainAspect = DomainAspect.Null,
    _ruleAspect = RuleAspect.Null,
    _actorAspect = ActorAspect.Null,
    _codecAspect = CodecAspect.Null
  )

}
