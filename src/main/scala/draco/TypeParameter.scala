package draco

import draco.format.dracon.Dracon

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeParameter extends DracoType {
  val name: String
  val upperBound: Dracon
  val lowerBound: Dracon
}

object TypeParameter extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeParameter", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeParameter] = Type[TypeParameter] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeParameter] = Encoder.instance { x =>
    val fields = Seq(
      Some("name" -> x.name.asJson),
      if (!x.upperBound.isNull && x.upperBound.textOrEmpty.fold(true, _.nonEmpty)) Some("upperBound" -> draco.scalatarget.CirceCodecs.encodePayload(x.upperBound)) else None,
      if (!x.lowerBound.isNull && x.lowerBound.textOrEmpty.fold(true, _.nonEmpty)) Some("lowerBound" -> draco.scalatarget.CirceCodecs.encodePayload(x.lowerBound)) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeParameter] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
      _upperBound <- cursor.downField("upperBound").as[Option[Json]].map(_.getOrElse(Json.Null)).flatMap(value => draco.scalatarget.CirceCodecs.decodePayload.decodeJson(value))
      _lowerBound <- cursor.downField("lowerBound").as[Option[Json]].map(_.getOrElse(Json.Null)).flatMap(value => draco.scalatarget.CirceCodecs.decodePayload.decodeJson(value))
    } yield TypeParameter (_name, _upperBound, _lowerBound)
  }

  def apply (
    _name: => String,
    _upperBound: => Dracon = Dracon.Null,
    _lowerBound: => Dracon = Dracon.Null
  ) : TypeParameter = new TypeParameter {
    override lazy val name: String = _name
    override lazy val upperBound: Dracon = _upperBound
    override lazy val lowerBound: Dracon = _lowerBound
    override lazy val typeDefinition: TypeDefinition = TypeParameter.typeDefinition
  }

  lazy val Null: TypeParameter = apply(
    _name = "",
    _upperBound = Dracon.Null,
    _lowerBound = Dracon.Null
  )


}
