package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait ActorAspect extends Extensible {
  val messageAction: Action
  val signalAction: Action
}

object ActorAspect {
  def apply(
             _messageAction: Action = Action.Null,
             _signalAction: Action = Action.Null
           ): ActorAspect = new ActorAspect {
    override lazy val messageAction: Action = if (_messageAction != null) _messageAction else Action.Null
    override lazy val signalAction: Action = if (_signalAction != null) _signalAction else Action.Null
  }

  lazy val Null: ActorAspect = apply()

  lazy val isEmpty: ActorAspect => Boolean = aa =>
    aa.messageAction.body.isEmpty && aa.signalAction.body.isEmpty

  implicit lazy val encoder: Encoder[ActorAspect] = Encoder.instance { aa =>
    val fields = Seq(
      if (aa.messageAction.body.nonEmpty) Some("messageAction" -> aa.messageAction.asJson) else None,
      if (aa.signalAction.body.nonEmpty) Some("signalAction" -> aa.signalAction.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }

  implicit lazy val decoder: Decoder[ActorAspect] = Decoder.instance { cursor =>
    for {
      _messageAction <- cursor.downField("messageAction").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signalAction  <- cursor.downField("signalAction").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield apply(_messageAction, _signalAction)
  }
}
