package domains.aerial

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait PositionReport extends Aerial with Dracon

object PositionReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("PositionReport", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[PositionReport] = Type[PositionReport] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  implicit lazy val encoder: Encoder[PositionReport] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[PositionReport] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield PositionReport (_json)
  }

  def apply (
    _json: => Json
  ) : PositionReport = new PositionReport {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = PositionReport.typeDefinition
  }

  lazy val Null: PositionReport = apply(
    _json = Json.Null
  )


}
