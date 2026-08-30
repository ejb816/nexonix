package draco.base

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Meters extends Distance[Double]

object Meters extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Meters", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Meters] = Type[Meters] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)

  implicit lazy val encoder: Encoder[Meters] = Encoder.instance { x =>
    val fields = Seq(
      Some("value" -> x.value.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Meters] = Decoder.instance { cursor =>
    for {
      _value <- cursor.downField("value").as[Double]
    } yield Meters (_value)
  }

  def apply (
    _value: Double
  ) : Meters = new Meters {
    override lazy val value: Double = _value
    override lazy val typeDefinition: TypeDefinition = Meters.typeDefinition
  }

  lazy val Null: Meters = apply(
    _value = 0.0
  )


}
