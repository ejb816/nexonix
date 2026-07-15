package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait RuleAspect extends DracoType {
  val pattern: Pattern
  val action: Action
}

object RuleAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("RuleAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[RuleAspect] = Type[RuleAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[RuleAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.pattern.variables.nonEmpty) Some("pattern" -> x.pattern.asJson) else None,
      if (x.action.body.nonEmpty) Some("action" -> x.action.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[RuleAspect] = Decoder.instance { cursor =>
    for {
      _pattern <- cursor.downField("pattern").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
      _action <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield RuleAspect (_pattern, _action)
  }

  def apply (
    _pattern: Pattern = Pattern.Null,
    _action: Action = Action.Null
  ) : RuleAspect = new RuleAspect {
    override lazy val pattern: Pattern = _pattern
    override lazy val action: Action = _action
    override lazy val typeDefinition: TypeDefinition = RuleAspect.typeDefinition
  }

  lazy val Null: RuleAspect = apply()

  lazy val isEmpty: RuleAspect => Boolean = ra => ra.pattern.variables.isEmpty && ra.pattern.conditions.isEmpty && ra.action.body.isEmpty
}
