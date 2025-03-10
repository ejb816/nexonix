package draco.domain.define

import cats.conversions.all.autoWidenFunctor
import draco.domain.{Dictionary, Domain, TypeData, TypeName}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import scala.language.implicitConversions

sealed trait DataDomainDictionary extends Dictionary[Domain[TypeData]]

object DataDomainDictionary {
  def apply (elements: (TypeName, Seq[TypeName])*) : DataDomainDictionary = {
    new DataDomainDictionary {
      override protected val internalMap: Map[String, Domain[TypeData]] = ???
    }
  }
  implicit val encoder: Encoder[DataDomainDictionary] = deriveEncoder
  implicit val decoder: Decoder[DataDomainDictionary] = deriveDecoder
}
