package draco.dreams.user

import draco.dreams._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Update extends Latent {
  val definition: draco.TypeDefinition
  val members: Seq[draco.TypeDefinition]
  def proposal(_accepted: => draco.DomainOntology): draco.DomainOntology = {
    lazy val accepted: draco.DomainOntology = _accepted
    accepted.including(draco.Domain(definition, members))
  }
  def subject(_accepted: => draco.DomainOntology): draco.TypeDefinition = {
    lazy val accepted: draco.DomainOntology = _accepted
    definition
  }
  def contents(_accepted: => draco.DomainOntology): Seq[draco.TypeDefinition] = {
    lazy val accepted: draco.DomainOntology = _accepted
    members
  }
}

object Update extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Update", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Update] = Type[Update] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)

  implicit lazy val encoder: Encoder[Update] = Encoder.instance { x =>
    val fields = Seq(
      Some("definition" -> x.definition.asJson),
      if (x.members.nonEmpty) Some("members" -> x.members.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Update] = Decoder.instance { cursor =>
    for {
      _definition <- cursor.downField("definition").as[draco.TypeDefinition]
      _members <- cursor.downField("members").as[Option[Seq[draco.TypeDefinition]]].map(_.getOrElse(Seq.empty))
    } yield Update (_definition, _members)
  }

  def apply (
    _definition: => draco.TypeDefinition,
    _members: => Seq[draco.TypeDefinition] = Seq.empty
  ) : Update = new Update {
    override lazy val definition: draco.TypeDefinition = _definition
    override lazy val members: Seq[draco.TypeDefinition] = _members
    override lazy val typeDefinition: TypeDefinition = Update.typeDefinition
  }

  lazy val Null: Update = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition],
    _members = Seq.empty
  )


}
