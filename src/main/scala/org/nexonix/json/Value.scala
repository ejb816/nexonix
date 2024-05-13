package org.nexonix.json

import io.circe.Json

trait Value[T] {
  val name: String
  val source: Json
  val pathElements: Array[String]
  val value : T
}

object Value {
  def create(_name: String, _source: Json, _pathElements: Array[String]) : Value[Json] = {
    new Value[Json] {
      override val name: String = _name
      override val source: Json = _source
      override val pathElements: Array[String] = _pathElements
      override val value: Json = pathElements.foldLeft(Option(_source)) {
        (e, a) => e.flatMap(j => j.hcursor.downField(a).focus)
      }.orNull
    }
  }
}
