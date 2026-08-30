package draco.base

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Radians extends Rotation[Double]

object Radians extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Radians", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Radians] = Type[Radians] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)

  implicit lazy val encoder: Encoder[Radians] = Encoder.instance { x =>
    val fields = Seq(
      Some("value" -> x.value.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Radians] = Decoder.instance { cursor =>
    for {
      _value <- cursor.downField("value").as[Double]
    } yield Radians (_value)
  }

  def apply (
    _value: Double
  ) : Radians = new Radians {
    override lazy val value: Double = _value
    override lazy val typeDefinition: TypeDefinition = Radians.typeDefinition
  }

  lazy val Null: Radians = apply(
    _value = 0.0
  )


}
