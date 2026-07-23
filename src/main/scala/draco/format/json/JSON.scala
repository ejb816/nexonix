package draco.format.json

import draco.format._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait JSON extends Format[JSON] {
  val json: Json
  lazy val value: JSON = this
}

object JSON extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("JSON", _namePackage = Seq ("draco", "format", "json")))
  lazy val dracoType: Type[JSON] = Type[JSON] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Value")

  implicit lazy val encoder: Encoder[JSON] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[JSON] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield JSON (_json)
  }

  lazy val domainType: Domain[JSON] = Domain[JSON] (typeDefinition)
  def apply (
    _json: Json
  ) : JSON = new JSON {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = JSON.typeDefinition
  }

  lazy val Null: JSON = apply(
    _json = Json.Null
  )

}
