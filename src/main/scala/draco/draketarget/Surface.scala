package draco.draketarget

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Surface extends Primal[String]

object Surface extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Surface", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[Surface] = Type[Surface] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  implicit lazy val encoder: Encoder[Surface] = Encoder.instance { x =>
    val fields = Seq(
      Some("value" -> x.value.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Surface] = Decoder.instance { cursor =>
    for {
      _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
    } yield Surface (_value)
  }

  def apply (
    _value: String
  ) : Surface = new Surface {
    override lazy val value: String = _value
    override lazy val typeDefinition: TypeDefinition = Surface.typeDefinition
  }

  lazy val Null: Surface = apply(
    _value = ""
  )


}
