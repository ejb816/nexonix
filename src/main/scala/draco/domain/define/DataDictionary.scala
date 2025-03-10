package draco.domain.define

import draco.domain.{Dictionary, TypeData}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

sealed trait DataDictionary extends Dictionary[TypeData]

object DataDictionary {
  def apply (elements: (String, TypeData)*) : DataDictionary = {
    new DataDictionary {
      override protected val internalMap: Map[String, TypeData] = Map(elements: _*)
    }
  }
  implicit val encoder: Encoder[DataDictionary] = deriveEncoder
  implicit val decoder: Decoder[DataDictionary] = deriveDecoder
}