
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait ActorDefinition extends TypeInstance {
  val typeName: TypeName
  val sourceDomain: TypeName
  val targetDomain: TypeName
  val messageAction: Action
  val signalAction: Action
}

object ActorDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("ActorDefinition", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[ActorDefinition] = Type[ActorDefinition] (typeDefinition)

  implicit lazy val encoder: Encoder[ActorDefinition] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeName" -> x.typeName.asJson),
      if (x.sourceDomain.name.nonEmpty) Some("sourceDomain" -> x.sourceDomain.asJson) else None,
      if (x.targetDomain.name.nonEmpty) Some("targetDomain" -> x.targetDomain.asJson) else None,
      if (x.messageAction.body.nonEmpty) Some("messageAction" -> x.messageAction.asJson) else None,
      if (x.signalAction.body.nonEmpty) Some("signalAction" -> x.signalAction.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[ActorDefinition] = Decoder.instance { cursor =>
    for {
      _typeName <- cursor.downField("typeName").as[TypeName]
      _sourceDomain <- cursor.downField("sourceDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _targetDomain <- cursor.downField("targetDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _messageAction <- cursor.downField("messageAction").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signalAction <- cursor.downField("signalAction").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield ActorDefinition (_typeName, _sourceDomain, _targetDomain, _messageAction, _signalAction)
  }

  def apply (
    _typeName: TypeName,
    _sourceDomain: TypeName = TypeName.Null,
    _targetDomain: TypeName = TypeName.Null,
    _messageAction: Action = Action.Null,
    _signalAction: Action = Action.Null
  ) : ActorDefinition = new ActorDefinition {
    override val typeName: TypeName = _typeName
    override val sourceDomain: TypeName = _sourceDomain
    override val targetDomain: TypeName = _targetDomain
    override val messageAction: Action = _messageAction
    override val signalAction: Action = _signalAction
    override lazy val typeInstance: DracoType = ActorDefinition.typeInstance
    override lazy val typeDefinition: TypeDefinition = ActorDefinition.typeDefinition
  }

  lazy val Null: ActorDefinition = apply(
    _typeName = null.asInstanceOf[TypeName],
    _sourceDomain = TypeName.Null,
    _targetDomain = TypeName.Null,
    _messageAction = Action.Null,
    _signalAction = Action.Null
  )


}
