package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait ActorAspect extends DracoType {
  val message: Action
  val signal: Action
  val start: Action
}

object ActorAspect extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ActorAspect", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[ActorAspect] = Type[ActorAspect] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[ActorAspect] = Encoder.instance { x =>
    val fields = Seq(
      if (x.message.body.nonEmpty) Some("message" -> x.message.asJson) else None,
      if (x.signal.body.nonEmpty) Some("signal" -> x.signal.asJson) else None,
      if (x.start.body.nonEmpty) Some("start" -> x.start.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[ActorAspect] = Decoder.instance { cursor =>
    for {
      _message <- cursor.downField("message").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signal <- cursor.downField("signal").as[Option[Action]].map(_.getOrElse(Action.Null))
      _start <- cursor.downField("start").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield ActorAspect (_message, _signal, _start)
  }

  def apply (
    _message: Action = Action.Null,
    _signal: Action = Action.Null,
    _start: Action = Action.Null
  ) : ActorAspect = new ActorAspect {
    override lazy val message: Action = _message
    override lazy val signal: Action = _signal
    override lazy val start: Action = _start
    override lazy val typeDefinition: TypeDefinition = ActorAspect.typeDefinition
  }

  lazy val Null: ActorAspect = apply()

  lazy val isEmpty: ActorAspect => Boolean = aa => aa.message.body.isEmpty && aa.signal.body.isEmpty && aa.start.body.isEmpty
}
