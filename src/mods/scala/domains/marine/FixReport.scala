package domains.marine

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait FixReport extends Marine with Dracon

object FixReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("FixReport", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[FixReport] = Type[FixReport] (typeDefinition)
  lazy val domainType: Domain[Marine] = Domain[Marine] (typeDefinition)

  implicit lazy val encoder: Encoder[FixReport] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[FixReport] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield FixReport (_json)
  }

  def apply (
    _json: => Json
  ) : FixReport = new FixReport {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = FixReport.typeDefinition
  }

  lazy val Null: FixReport = apply(
    _json = Json.Null
  )


}
