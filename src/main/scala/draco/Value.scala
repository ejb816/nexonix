package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait Value {
  val name: String
  val pathElements: Seq[String]
  def value[T] (_source: Json)(implicit decoder: Decoder[T]): T = {
    val pathValue: Json = pathElements.foldLeft(Option(_source)) {
      (e, a) => e.flatMap(j => if (j.isArray) j.hcursor.downN(a.toInt).focus else j.hcursor.downField(a).focus)
    }.orNull
    if (pathValue != null) pathValue.as[T].getOrElse(null.asInstanceOf[T])
    else null.asInstanceOf[T]
  }}

object Value extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Value",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("name", "String"),
      Fixed ("pathElements", "Seq[String]")
    ),
    _factory = Factory (
      "Value",
      _parameters = Seq (
        Parameter ("name", "String", ""),
        Parameter ("pathElements", "Seq[String]", "")
      )
    )
  )
  lazy val typeInstance: Type[Value] = Type[Value] (typeDefinition)

  def apply(_name: String, _pathElements: Seq[String]): Value = {
    new Value {
      override val name: String = _name
      override val pathElements: Seq[String] = _pathElements
    }
  }

  implicit lazy val encoder: Encoder[Value] = Encoder.instance { v =>
    val fields = Seq(
      if (v.name.nonEmpty) Some("name" -> v.name.asJson) else None,
      if (v.pathElements.nonEmpty) Some("pathElements" -> v.pathElements.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  implicit lazy val decoder: Decoder[Value] = Decoder.instance { cursor =>
    for {
      _name         <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
      _pathElements <- cursor.downField("pathElements").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield Value (
      _name,
      _pathElements
    )
  }
}