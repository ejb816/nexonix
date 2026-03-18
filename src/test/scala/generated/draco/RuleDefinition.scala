
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait RuleDefinition  {
  val typeName: TypeName
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
  val values: Seq[Value]
  val action: Action
}

object RuleDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("RuleDefinition", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[RuleDefinition] = Type[RuleDefinition] (typeDefinition)

  implicit lazy val encoder: Encoder[RuleDefinition] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeName" -> x.typeName.asJson),
      if (x.variables.nonEmpty) Some("variables" -> x.variables.asJson) else None,
      if (x.conditions.nonEmpty) Some("conditions" -> x.conditions.asJson) else None,
      if (x.values.nonEmpty) Some("values" -> x.values.asJson) else None,
      if (x.action.body.nonEmpty) Some("action" -> x.action.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[RuleDefinition] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[TypeName]
      _variables <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _action <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield RuleDefinition (_typeName, _variables, _conditions, _values, _action)
  }

  def apply (
    _typeName: TypeName,
    _variables: Seq[Variable] = Seq.empty,
    _conditions: Seq[Condition] = Seq.empty,
    _values: Seq[Value] = Seq.empty,
    _action: Action = Action.Null
  ) : RuleDefinition = new RuleDefinition {
    override val typeName: TypeName = _typeName
    override val variables: Seq[Variable] = _variables
    override val conditions: Seq[Condition] = _conditions
    override val values: Seq[Value] = _values
    override val action: Action = _action
    override lazy val typeInstance: DracoType = RuleDefinition.typeInstance
    override lazy val typeDefinition: TypeDefinition = RuleDefinition.typeDefinition
  }

  lazy val Null: RuleDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _variables = Seq.empty,
    _conditions = Seq.empty,
    _values = Seq.empty,
    _action = Action.Null
  )


}
