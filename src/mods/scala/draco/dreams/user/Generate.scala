package draco.dreams.user

import draco.dreams._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Generate extends Request {
  val domain: draco.TypeName
  val transform: draco.TypeName
  def proposal(_accepted: => draco.DomainOntology): draco.DomainOntology = {
    lazy val accepted: draco.DomainOntology = _accepted
    accepted
  }
  def subject(_accepted: => draco.DomainOntology): draco.TypeDefinition = {
    lazy val accepted: draco.DomainOntology = _accepted
    accepted.holding(domain).fold(draco.TypeDefinition(domain), d => d.typeDefinition)
  }
  def contents(_accepted: => draco.DomainOntology): Seq[draco.TypeDefinition] = {
    lazy val accepted: draco.DomainOntology = _accepted
    accepted.holding(domain).fold(Seq.empty, d => d.typeDictionary.elementTypes)
  }
}

object Generate extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Generate", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Generate] = Type[Generate] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)

  implicit lazy val encoder: Encoder[Generate] = Encoder.instance { x =>
    val fields = Seq(
      Some("domain" -> x.domain.asJson),
      Some("transform" -> x.transform.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Generate] = Decoder.instance { cursor =>
    for {
      _domain <- cursor.downField("domain").as[draco.TypeName]
      _transform <- cursor.downField("transform").as[draco.TypeName]
    } yield Generate (_domain, _transform)
  }

  def apply (
    _domain: => draco.TypeName,
    _transform: => draco.TypeName
  ) : Generate = new Generate {
    override lazy val domain: draco.TypeName = _domain
    override lazy val transform: draco.TypeName = _transform
    override lazy val typeDefinition: TypeDefinition = Generate.typeDefinition
  }

  lazy val Null: Generate = apply(
    _domain = null.asInstanceOf[draco.TypeName],
    _transform = null.asInstanceOf[draco.TypeName]
  )


}
