package scenario.ash

import draco._
import scenario._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Compound extends Primal[String]

object Compound extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Compound", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[Compound] = Type[Compound] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)

  implicit lazy val encoder: Encoder[Compound] = Encoder.instance { x =>
    val fields = Seq(
      Some("value" -> x.value.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Compound] = Decoder.instance { cursor =>
    for {
      _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
    } yield Compound (_value)
  }

  def apply (
    _value: String
  ) : Compound = new Compound {
    override lazy val value: String = _value
    override lazy val typeDefinition: TypeDefinition = Compound.typeDefinition
  }

  lazy val Null: Compound = apply(
    _value = ""
  )


}
