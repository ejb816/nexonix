package draco

import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._

trait ActorDefinition extends TypeInstance {
  val typeName: TypeName
  val sourceDomain: TypeName
  val targetDomain: TypeName
  val messageAction: Action
  val signalAction: Action
}

object ActorDefinition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "ActorDefinition",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("sourceDomain", "TypeName"),
      Fixed ("targetDomain", "TypeName"),
      Fixed ("messageAction", "Action"),
      Fixed ("signalAction", "Action")
    ),
    _factory = Factory (
      "ActorDefinition",
      _parameters = Seq (
        Parameter ("typeName", "TypeName", ""),
        Parameter ("sourceDomain", "TypeName", "TypeName.Null"),
        Parameter ("targetDomain", "TypeName", "TypeName.Null"),
        Parameter ("messageAction", "Action", "Action.Null"),
        Parameter ("signalAction", "Action", "Action.Null")
      )
    )
  )
  lazy val typeInstance: Type[ActorDefinition] = Type[ActorDefinition] (typeDefinition)

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
    override val typeInstance: DracoType = ActorDefinition.typeInstance
    override val typeDefinition: TypeDefinition = ActorDefinition.typeDefinition
  }

  lazy val Null: ActorDefinition = ActorDefinition (TypeName.Null)

  lazy implicit val encoder: Encoder[ActorDefinition] = Encoder.instance { m =>
    val fields = Seq(
      Some("typeName" -> m.typeName.asJson),
      if (m.sourceDomain.name.nonEmpty) Some("sourceDomain" -> m.sourceDomain.asJson) else None,
      if (m.targetDomain.name.nonEmpty) Some("targetDomain" -> m.targetDomain.asJson) else None,
      if (m.messageAction.body.nonEmpty) Some("messageAction" -> m.messageAction.asJson) else None,
      if (m.signalAction.body.nonEmpty) Some("signalAction" -> m.signalAction.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  lazy implicit val decoder: Decoder[ActorDefinition] = Decoder.instance { c =>
    for {
      _typeName      <- c.downField("typeName").as[TypeName]
      _sourceDomain  <- c.downField("sourceDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _targetDomain  <- c.downField("targetDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _messageAction <- c.downField("messageAction").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signalAction  <- c.downField("signalAction").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield ActorDefinition (_typeName, _sourceDomain, _targetDomain, _messageAction, _signalAction)
  }
}
