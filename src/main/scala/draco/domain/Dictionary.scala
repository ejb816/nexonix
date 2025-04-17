package draco.domain

import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json, JsonObject}

trait Dictionary[V] extends KeyValueMap[String, V] {
  implicit val dictionaryEncoder : Encoder[Dictionary[V]] = (d: Dictionary[V]) => d.asJson
  implicit val encoder : Encoder[V] = (d: V) => d.asJson
  def asJson: Json = {
    kvMap.foldLeft(Json.obj()) {
      case (json, (key, value)) =>
        json.deepMerge(JsonObject(key -> value.asJson).asJson)
    }
  }
}

object Dictionary {
  def apply[V](elements: (String, V)*): Dictionary[V] = {
    new Dictionary[V] {
      val kvMap: Map[String, V] = Map(elements: _*)
    }
  }
}
