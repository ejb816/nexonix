package org.nexonix.json

import io.circe.Json

trait Value {
  val name: String
  val source: Json
  val pathElements: Array[String]
  val value : Json
}

object Value {
  def define(_name: String, _source: Json, _pathElements: Array[String]) : Value = {
    new Value {
      override val name: String = _name
      override val source: Json = _source
      override val pathElements: Array[String] = _pathElements
      override val value: Json = pathElements.foldLeft(Option(source)) {
        (e, a) => e.flatMap(j => if (j.isArray) j.hcursor.downN(a.toInt).focus else j.hcursor.downField(a).focus)
      }.orNull
    }
  }
}
