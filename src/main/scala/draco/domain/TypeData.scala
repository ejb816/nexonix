package draco.domain

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}

trait TypeData {
  val definition: TypeDefinition = TypeDefinition(
    _typeName = TypeName(_name = "TypeData", _namePackage = Seq("draco", "domain"))
  )
}

object TypeData {
  def apply (_definition: TypeDefinition) : TypeData = {
    new TypeData {
      override val definition: TypeDefinition = _definition
    }
  }

  implicit val encoder: Encoder[TypeData] = { domain =>
    Json.obj(
      "definition" -> domain.definition.asJson
    )
  }

  implicit val decoder: Decoder[TypeData] = (c: HCursor) => for {
    _definition <- c.downField("definition").as[TypeDefinition]
  } yield TypeData (_definition)
}
