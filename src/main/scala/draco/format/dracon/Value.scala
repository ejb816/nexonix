package draco.format.dracon

import draco.drake.Absent
import draco.drake.Presence

import draco.format._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Value extends draco.format.Value[Dracon] {
  def decode[T](_source: => Dracon, _decoder: => draco.format.dracon.Decoder[T]): Presence[T] = {
    lazy val source: Dracon = _source
    lazy val decoder: draco.format.dracon.Decoder[T] = _decoder
    value(source).fold(Absent(), selected => decoder.decode(selected))
  }
}

object Value extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Value", _namePackage = Seq ("draco", "format", "dracon")))
  lazy val dracoType: Type[Value] = Type[Value] (typeDefinition)
  lazy val domainType: Domain[Dracon] = Domain[Dracon] (typeDefinition)

  implicit lazy val encoder: Encoder[Value] = Encoder.instance { x =>
    val fields = Seq(
      Some("name" -> x.name.asJson),
      Some("pathElements" -> x.pathElements.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Value] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
      _pathElements <- cursor.downField("pathElements").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield Value (_name, _pathElements)
  }

  def apply (
    _name: => String,
    _pathElements: => Seq[String]
  ) : Value = new Value {
    override lazy val name: String = _name
    override lazy val pathElements: Seq[String] = _pathElements
    override def value(_source: => Dracon): Presence[Dracon] = {
      lazy val source: Dracon = _source
      source.select(pathElements)
    }
    override lazy val typeDefinition: TypeDefinition = Value.typeDefinition
  }

  lazy val Null: Value = apply(
    _name = "",
    _pathElements = Seq.empty
  )


}
