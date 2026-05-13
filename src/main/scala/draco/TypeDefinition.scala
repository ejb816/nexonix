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

  def apply (
    _typeName: TypeName,
    _dracoAspect: DracoAspect = DracoAspect.Null,
    _domainAspect: DomainAspect = DomainAspect.Null,
    _ruleAspect: RuleAspect = RuleAspect.Null,
    _actorAspect: ActorAspect = ActorAspect.Null
  ) : TypeDefinition = new TypeDefinition {
    override lazy val typeName: TypeName = _typeName
    override lazy val dracoAspect: DracoAspect = _dracoAspect
    override lazy val domainAspect: DomainAspect = _domainAspect
    override lazy val ruleAspect: RuleAspect = _ruleAspect
    override lazy val actorAspect: ActorAspect = _actorAspect
    override lazy val typeDefinition: TypeDefinition = TypeDefinition.typeDefinition
  }

  lazy val Null: TypeDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _dracoAspect = DracoAspect.Null,
    _domainAspect = DomainAspect.Null,
    _ruleAspect = RuleAspect.Null,
    _actorAspect = ActorAspect.Null
  )

  implicit lazy val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    val fields = Seq(
      Some("typeName" -> td.typeName.asJson),
      if (!DracoAspect.isEmpty(td.dracoAspect))   Some("dracoAspect"  -> td.dracoAspect.asJson)  else None,
      if (!DomainAspect.isEmpty(td.domainAspect)) Some("domainAspect" -> td.domainAspect.asJson) else None,
      if (!RuleAspect.isEmpty(td.ruleAspect))     Some("ruleAspect"   -> td.ruleAspect.asJson)   else None,
      if (!ActorAspect.isEmpty(td.actorAspect))   Some("actorAspect"  -> td.actorAspect.asJson)  else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName     <- cursor.downField("typeName").as[TypeName]
      _dracoAspect  <- cursor.downField("dracoAspect").as[Option[DracoAspect]].map(_.getOrElse(DracoAspect.Null))
      _domainAspect <- cursor.downField("domainAspect").as[Option[DomainAspect]].map(_.getOrElse(DomainAspect.Null))
      _ruleAspect   <- cursor.downField("ruleAspect").as[Option[RuleAspect]].map(_.getOrElse(RuleAspect.Null))
      _actorAspect  <- cursor.downField("actorAspect").as[Option[ActorAspect]].map(_.getOrElse(ActorAspect.Null))
    } yield apply(_typeName, _dracoAspect, _domainAspect, _ruleAspect, _actorAspect)
  }
}
