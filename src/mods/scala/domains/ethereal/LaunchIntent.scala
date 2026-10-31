package domains.ethereal

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait LaunchIntent extends Ethereal with Dracon

object LaunchIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("LaunchIntent", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[LaunchIntent] = Type[LaunchIntent] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)

  implicit lazy val encoder: Encoder[LaunchIntent] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[LaunchIntent] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield LaunchIntent (_json)
  }

  def apply (
    _json: => Json
  ) : LaunchIntent = new LaunchIntent {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = LaunchIntent.typeDefinition
  }

  lazy val Null: LaunchIntent = apply(
    _json = Json.Null
  )


}
