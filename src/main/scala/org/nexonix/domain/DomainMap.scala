package org.nexonix.domain


import io.circe.{Encoder, Json, JsonObject}
import io.circe.syntax.EncoderOps


trait DomainMap[T] extends Map[String, T] {
  implicit val encoder: Encoder[T]

  protected val internalMap: Map[String, T]

  def removed(key: String): Map[String, T] = internalMap.removed(key)

  def updated[V1 >: T](key: String, value: V1): Map[String, V1] = internalMap.updated(key, value)

  def get(key: String): Option[T] = internalMap.get(key)

  def iterator: Iterator[(String, T)] = internalMap.iterator

  def asJson: Json = {
    internalMap.foldLeft(Json.obj()) {
      case (json, (key, value)) =>
        json.deepMerge(JsonObject(key -> value.asJson).asJson)
    }
  }
}

