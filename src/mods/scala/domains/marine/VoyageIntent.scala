package domains.marine

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait VoyageIntent extends Marine with Dracon

object VoyageIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("VoyageIntent", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[VoyageIntent] = Type[VoyageIntent] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  implicit lazy val encoder: Encoder[VoyageIntent] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[VoyageIntent] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield VoyageIntent (_json)
  }

  def apply (
    _json: => Json
  ) : VoyageIntent = new VoyageIntent {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = VoyageIntent.typeDefinition
  }

  lazy val Null: VoyageIntent = apply(
    _json = Json.Null
  )


}
