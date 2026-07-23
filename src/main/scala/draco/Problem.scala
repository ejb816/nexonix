package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Problem extends DracoType {
  val subject: TypeName
  val message: String
}

object Problem extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Problem", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Problem] = Type[Problem] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[Problem] = Encoder.instance { x =>
    val fields = Seq(
      Some("subject" -> x.subject.asJson),
      Some("message" -> x.message.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Problem] = Decoder.instance { cursor =>
    for {
      _subject <- cursor.downField("subject").as[TypeName]
      _message <- cursor.downField("message").as[Option[String]].map(_.getOrElse(""))
    } yield Problem (_subject, _message)
  }

  def apply (
    _subject: TypeName,
    _message: String
  ) : Problem = new Problem {
    override lazy val subject: TypeName = _subject
    override lazy val message: String = _message
    override lazy val typeDefinition: TypeDefinition = Problem.typeDefinition
  }

  lazy val Null: Problem = apply(
    _subject = null.asInstanceOf[TypeName],
    _message = ""
  )


}
