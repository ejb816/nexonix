package draco.format.dracon

import draco.format._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Dracon extends Format[Dracon] {
  val json: Json
  lazy val value: Dracon = this
  def select(_pathElements: => Seq[String]): draco.drake.Presence[Dracon] = {
    lazy val pathElements: Seq[String] = _pathElements
    lazy val selected: Option[Json] = pathElements.foldLeft(Option(json))((current, key) => current.flatMap(node => (if (node.isArray) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(key.toIntOption.filter(index => index >= 0).flatMap(index => node.hcursor.downN(index).focus), node.hcursor.downField(key).focus)))
    (selected.map(node => Dracon(node)) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
  }
}

object Dracon extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Dracon", _namePackage = Seq ("draco", "format", "dracon")))
  lazy val dracoType: Type[Dracon] = Type[Dracon] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Decoder", "Encoder", "Value")

  implicit lazy val encoder: Encoder[Dracon] = Encoder.instance { x =>
    val fields = Seq(
      Some("json" -> x.json.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Dracon] = Decoder.instance { cursor =>
    for {
      _json <- cursor.downField("json").as[Option[Json]].map(_.getOrElse(Json.Null))
    } yield Dracon (_json)
  }

  lazy val domainType: Domain[Dracon] = Domain[Dracon] (typeDefinition)
  def apply (
    _json: => Json
  ) : Dracon = new Dracon {
    override lazy val json: Json = _json
    override lazy val typeDefinition: TypeDefinition = Dracon.typeDefinition
  }

  lazy val Null: Dracon = apply(
    _json = Json.Null
  )

}
