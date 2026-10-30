package domains.ethereal

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait EphemerisReport extends Ethereal with Dracon

object EphemerisReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("EphemerisReport", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[EphemerisReport] = Type[EphemerisReport] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)

  implicit lazy val encoder: Encoder[EphemerisReport] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[EphemerisReport] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield EphemerisReport (_json)
  }

  def apply (
    _json: => Json
  ) : EphemerisReport = new EphemerisReport {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = EphemerisReport.typeDefinition
  }

  lazy val Null: EphemerisReport = apply(
    _json = Json.Null
  )


}
