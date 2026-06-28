package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Binding extends DracoType {
  val from: TypeName
  val param: String
  val to: TypeName
}

object Binding extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Binding", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Binding] = Type[Binding] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[Binding] = Encoder.instance { x =>
    val fields = Seq(
      if (x.from.name.nonEmpty) Some("from" -> x.from.asJson) else None,
      if (x.param.nonEmpty) Some("param" -> x.param.asJson) else None,
      if (x.to.name.nonEmpty) Some("to" -> x.to.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Binding] = Decoder.instance { cursor =>
    for {
      _from <- cursor.downField("from").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _param <- cursor.downField("param").as[Option[String]].map(_.getOrElse(""))
      _to <- cursor.downField("to").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield Binding (_from, _param, _to)
  }

  def apply (
    _from: TypeName = TypeName.Null,
    _param: String = "",
    _to: TypeName = TypeName.Null
  ) : Binding = new Binding {
    override lazy val from: TypeName = _from
    override lazy val param: String = _param
    override lazy val to: TypeName = _to
    override lazy val typeDefinition: TypeDefinition = Binding.typeDefinition
  }

  lazy val Null: Binding = apply()

}
