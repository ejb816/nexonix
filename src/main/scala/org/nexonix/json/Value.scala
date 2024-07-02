package org.nexonix.json


import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

trait Value {
  val name: String
  val source: Json
  val pathElements: Array[String]
  val pathValue : Json
  def value[T](implicit decoder: Decoder[T]): T = {
    if (pathValue != null) pathValue.as[T].getOrElse(null.asInstanceOf[T])
    else null.asInstanceOf[T]
  }}

object Value {
  def create(_name: String, _source: Json, _pathElements: Array[String]) : Value = {
    new Value {
      override val name: String = _name
      override val source: Json = _source
      override val pathElements: Array[String] = _pathElements
      override val pathValue: Json = pathElements.foldLeft(Option(_source)) {
        (e, a) => e.flatMap(j => if (j.isArray) j.hcursor.downN(a.toInt).focus else j.hcursor.downField(a).focus)
      }.orNull
    }
  }

  implicit val encoder: Encoder[Value] = (a: Value) => Json.obj(
    ("source", a.source),
    ("pathElements", a.pathElements.asJson)
  )
}
