package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait Value {
  val name: String
  val source: Json
  val pathElements: Seq[String]
  val pathValue : Json
  def value[T](implicit decoder: Decoder[T]): T = {
    if (pathValue != null) pathValue.as[T].getOrElse(null.asInstanceOf[T])
    else null.asInstanceOf[T]
  }
}

object Value {
  def apply (
              _name: String,
              _source: Json,
              _pathElements: Seq[String]
            ) : Value = {
    new Value {
      override val name: String = _name
      override val source: Json = _source
      override val pathElements: Seq[String] = _pathElements
      override val pathValue: Json = pathElements.foldLeft(Option(_source)) {
        (e, a) => e.flatMap(j => if (j.isArray) j.hcursor.downN(a.toInt).focus else j.hcursor.downField(a).focus)
      }.orNull
    }
  }

  implicit val encoder: Encoder[Value] = (a: Value) => Json.obj(
    ("name", a.name.asJson),
    ("source", a.source.asJson),
    ("pathElements", a.pathElements.asJson),
    ("pathValue", a.pathValue.asJson)
  )

  implicit val decoder: Decoder[Value] = Decoder.instance { c =>
    for {
      _name
        <- c.downField("name").as[String]
      _source
        <- c.downField("source").as[Json]
      _pathElements
        <- c.downField("pathElements").as[Array[String]]
    } yield Value(
      _name,
      _source,
      _pathElements
    )
  }
}
