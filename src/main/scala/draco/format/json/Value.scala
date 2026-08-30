package draco.format.json

import draco.format._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Value extends draco.format.Value[JSON]

object Value extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Value", _namePackage = Seq ("draco", "format", "json")))
  lazy val dracoType: Type[Value] = Type[Value] (typeDefinition)
  lazy val domainType: Domain[JSON] = Domain[JSON] (typeDefinition)

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
    _name: String,
    _pathElements: Seq[String]
  ) : Value = new Value {
    override lazy val name: String = _name
    override lazy val pathElements: Seq[String] = _pathElements
    override def value[T: Decoder](_source: JSON): T = {
      val pathValue: Json = pathElements.foldLeft(Option(_source.json))((e, a) => e.flatMap(j => if (j.isArray) j.hcursor.downN(a.toInt).focus else j.hcursor.downField(a).focus)).orNull
      if (pathValue != null) pathValue.as[T].getOrElse(null.asInstanceOf[T]) else null.asInstanceOf[T]
    }
    override lazy val typeDefinition: TypeDefinition = Value.typeDefinition
  }

  lazy val Null: Value = apply(
    _name = "",
    _pathElements = Seq.empty
  )


}
