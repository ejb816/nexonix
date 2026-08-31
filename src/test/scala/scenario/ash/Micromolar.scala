package scenario.ash

import draco._
import scenario._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Micromolar extends Primal[Double]

object Micromolar extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Micromolar", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[Micromolar] = Type[Micromolar] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)

  implicit lazy val encoder: Encoder[Micromolar] = Encoder.instance { x =>
    val fields = Seq(
      Some("value" -> x.value.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Micromolar] = Decoder.instance { cursor =>
    for {
      _value <- cursor.downField("value").as[Double]
    } yield Micromolar (_value)
  }

  def apply (
    _value: Double
  ) : Micromolar = new Micromolar {
    override lazy val value: Double = _value
    override lazy val typeDefinition: TypeDefinition = Micromolar.typeDefinition
  }

  lazy val Null: Micromolar = apply(
    _value = 0.0
  )


}
