package draco.generator

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Emission extends Primal[String]

object Emission extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Emission", _namePackage = Seq ("draco", "generator")))
  lazy val dracoType: Type[Emission] = Type[Emission] (typeDefinition)
  lazy val domainType: Domain[draco.generator.Generator] = Domain[draco.generator.Generator] (typeDefinition)

  implicit lazy val encoder: Encoder[Emission] = Encoder.instance { x =>
    val fields = Seq(
      Some("value" -> x.value.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Emission] = Decoder.instance { cursor =>
    for {
      _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
    } yield Emission (_value)
  }

  def apply (
    _value: String
  ) : Emission = new Emission {
    override lazy val value: String = _value
    override lazy val typeDefinition: TypeDefinition = Emission.typeDefinition
  }

  lazy val Null: Emission = apply(
    _value = ""
  )


}
