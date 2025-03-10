package draco.domain.define

import draco.domain.Dictionary
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait TransformDomainDictionary extends Dictionary[TransformDomain]

object TransformDomainDictionary {
  def apply (elements: (String, TransformDomain)*) : TransformDomainDictionary = {
    new TransformDomainDictionary {
      override protected val internalMap: Map[String, TransformDomain] = Map(elements: _*)
    }
  }
  implicit val encoder: Encoder[TransformDomainDictionary] = deriveEncoder
  implicit val decoder: Decoder[TransformDomainDictionary] = deriveDecoder
}