package draco.dreams.user

import draco.dreams._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Delete extends Request {
  val domain: draco.TypeName
  def proposal(_accepted: => draco.DomainOntology): draco.DomainOntology = {
    lazy val accepted: draco.DomainOntology = _accepted
    accepted.excluding(domain)
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

object Delete extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Delete", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Delete] = Type[Delete] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)

  implicit lazy val encoder: Encoder[Delete] = Encoder.instance { x =>
    val fields = Seq(
      Some("domain" -> x.domain.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Delete] = Decoder.instance { cursor =>
    for {
      _domain <- cursor.downField("domain").as[draco.TypeName]
    } yield Delete (_domain)
  }

  def apply (
    _domain: => draco.TypeName
  ) : Delete = new Delete {
    override lazy val domain: draco.TypeName = _domain
    override lazy val typeDefinition: TypeDefinition = Delete.typeDefinition
  }

  lazy val Null: Delete = apply(
    _domain = null.asInstanceOf[draco.TypeName]
  )


}
