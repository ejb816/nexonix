package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait RuleAspect extends DracoType {
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
  val values: Seq[Value]
  val pattern: Pattern
  val action: Action
}

object RuleAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("RuleAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[RuleAspect] = Type[RuleAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[RuleAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.variables.nonEmpty) Some("variables" -> x.variables.asJson) else None,
      if (x.conditions.nonEmpty) Some("conditions" -> x.conditions.asJson) else None,
      if (x.values.nonEmpty) Some("values" -> x.values.asJson) else None,
      if (x.pattern.variables.nonEmpty) Some("pattern" -> x.pattern.asJson) else None,
      if (x.action.body.nonEmpty) Some("action" -> x.action.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[RuleAspect] = Decoder.instance { cursor =>
    for {
      _variables <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _pattern <- cursor.downField("pattern").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
      _action <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield RuleAspect (_variables, _conditions, _values, _pattern, _action)
  }

  def apply (
    _variables: Seq[Variable] = Seq.empty,
    _conditions: Seq[Condition] = Seq.empty,
    _values: Seq[Value] = Seq.empty,
    _pattern: Pattern = Pattern.Null,
    _action: Action = Action.Null
  ) : RuleAspect = new RuleAspect {
    override lazy val variables: Seq[Variable] = _variables
    override lazy val conditions: Seq[Condition] = _conditions
    override lazy val values: Seq[Value] = _values
    override lazy val pattern: Pattern = _pattern
    override lazy val action: Action = _action
    override lazy val typeDefinition: TypeDefinition = RuleAspect.typeDefinition
  }

  lazy val Null: RuleAspect = apply()

  lazy val isEmpty: RuleAspect => Boolean = ra =>
    ra.variables.isEmpty &&
    ra.conditions.isEmpty &&
    ra.values.isEmpty &&
    ra.pattern.variables.isEmpty && ra.pattern.conditions.isEmpty &&
    ra.action.body.isEmpty
}
