package draco.domain.define

import draco.domain.{Base, Dictionary, Domain, TypeData, TypeName}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

sealed trait DataDomain extends Domain[TypeData]

object DataDomain {
  def apply (
              typeName: TypeName,
              domains: Dictionary[Domain[TypeData]] = Dictionary[Domain[TypeData]](),
            ) : DataDomain = {
    new DataDomain {
      override val superDomain: Domain[TypeData] = domains(typeName.name)
      override val subDomains: Dictionary[Domain[TypeData]] = domains
      override val base: Domain[TypeData] = Base.base
    }
  }
  implicit val encoder: Encoder[Domain[TypeData]] = deriveEncoder
  implicit val decoder: Decoder[Domain[TypeData]] = deriveDecoder
}
