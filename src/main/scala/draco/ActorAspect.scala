package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait ActorAspect extends DracoType {
  val messageAction: Action
  val signalAction: Action
}

object ActorAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("ActorAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[ActorAspect] = Type[ActorAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[ActorAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.messageAction.body.nonEmpty) Some("messageAction" -> x.messageAction.asJson) else None,
      if (x.signalAction.body.nonEmpty) Some("signalAction" -> x.signalAction.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[ActorAspect] = Decoder.instance { cursor =>
    for {
      _messageAction <- cursor.downField("messageAction").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signalAction <- cursor.downField("signalAction").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield ActorAspect (_messageAction, _signalAction)
  }

  def apply (
    _messageAction: Action = Action.Null,
    _signalAction: Action = Action.Null
  ) : ActorAspect = new ActorAspect {
    override lazy val messageAction: Action = _messageAction
    override lazy val signalAction: Action = _signalAction
    override lazy val typeDefinition: TypeDefinition = ActorAspect.typeDefinition
  }

  lazy val Null: ActorAspect = apply()

  lazy val isEmpty: ActorAspect => Boolean = aa =>
    aa.messageAction.body.isEmpty && aa.signalAction.body.isEmpty
}
