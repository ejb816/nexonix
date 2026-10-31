package domains.terrestrial

import draco._
import domains._
import draco.format.dracon._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait MarchIntent extends Terrestrial with Dracon

object MarchIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("MarchIntent", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[MarchIntent] = Type[MarchIntent] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  implicit lazy val encoder: Encoder[MarchIntent] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[MarchIntent] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield MarchIntent (_json)
  }

  def apply (
    _json: => Json
  ) : MarchIntent = new MarchIntent {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = MarchIntent.typeDefinition
  }

  lazy val Null: MarchIntent = apply(
    _json = Json.Null
  )


}
