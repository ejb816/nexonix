package draco.dreams.editor

import draco.generator.Emission

import draco.dreams._
import draco._
import draco.dreams.user._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Generated extends Response {
  val domain: draco.TypeName
  val transform: draco.TypeName
  val emissions: Seq[Emission]
}

object Generated extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Generated", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[Generated] = Type[Generated] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  implicit lazy val encoder: Encoder[Generated] = Encoder.instance { x =>
    val fields = Seq(
      Some("domain" -> x.domain.asJson),
      Some("transform" -> x.transform.asJson),
      if (x.emissions.nonEmpty) Some("emissions" -> x.emissions.asJson) else None,
      if (x.problems.nonEmpty) Some("problems" -> x.problems.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Generated] = Decoder.instance { cursor =>
    for {
      _domain <- cursor.downField("domain").as[draco.TypeName]
      _transform <- cursor.downField("transform").as[draco.TypeName]
      _emissions <- cursor.downField("emissions").as[Option[Seq[Emission]]].map(_.getOrElse(Seq.empty))
      _problems <- cursor.downField("problems").as[Option[Seq[draco.Problem]]].map(_.getOrElse(Seq.empty))
    } yield Generated (_domain, _transform, _emissions, _problems)
  }

  def apply (
    _domain: => draco.TypeName,
    _transform: => draco.TypeName,
    _emissions: => Seq[Emission] = Seq.empty,
    _problems: => Seq[draco.Problem] = Seq.empty
  ) : Generated = new Generated {
    override lazy val domain: draco.TypeName = _domain
    override lazy val transform: draco.TypeName = _transform
    override lazy val emissions: Seq[Emission] = _emissions
    override lazy val problems: Seq[draco.Problem] = _problems
    override lazy val typeDefinition: TypeDefinition = Generated.typeDefinition
  }

  lazy val Null: Generated = apply(
    _domain = null.asInstanceOf[draco.TypeName],
    _transform = null.asInstanceOf[draco.TypeName],
    _emissions = Seq.empty,
    _problems = Seq.empty
  )


}
