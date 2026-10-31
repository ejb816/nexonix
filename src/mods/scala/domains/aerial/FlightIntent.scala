package domains.aerial

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait FlightIntent extends Aerial with Dracon

object FlightIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FlightIntent", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[FlightIntent] = Type[FlightIntent] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  implicit lazy val encoder: Encoder[FlightIntent] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[FlightIntent] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield FlightIntent (_json)
  }

  def apply (
    _json: => Json
  ) : FlightIntent = new FlightIntent {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = FlightIntent.typeDefinition
  }

  lazy val Null: FlightIntent = apply(
    _json = Json.Null
  )


}
