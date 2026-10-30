package domains.terrestrial

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait LocationReport extends Terrestrial with Dracon

object LocationReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("LocationReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[LocationReport] = Type[LocationReport] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  implicit lazy val encoder: Encoder[LocationReport] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[LocationReport] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield LocationReport (_json)
  }

  def apply (
    _json: => Json
  ) : LocationReport = new LocationReport {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = LocationReport.typeDefinition
  }

  lazy val Null: LocationReport = apply(
    _json = Json.Null
  )


}
