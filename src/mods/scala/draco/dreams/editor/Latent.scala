package draco.dreams.editor

import draco.dreams._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Latent extends DracoType {
  val definition: draco.TypeDefinition
  val members: Seq[draco.TypeDefinition]
}

object Latent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Latent", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[Latent] = Type[Latent] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  implicit lazy val encoder: Encoder[Latent] = Encoder.instance { x =>
    val fields = Seq(
      Some("definition" -> x.definition.asJson),
      if (x.members.nonEmpty) Some("members" -> x.members.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Latent] = Decoder.instance { cursor =>
    for {
      _definition <- cursor.downField("definition").as[draco.TypeDefinition]
      _members <- cursor.downField("members").as[Option[Seq[draco.TypeDefinition]]].map(_.getOrElse(Seq.empty))
    } yield Latent (_definition, _members)
  }

  def apply (
    _definition: => draco.TypeDefinition,
    _members: => Seq[draco.TypeDefinition] = Seq.empty
  ) : Latent = new Latent {
    override lazy val definition: draco.TypeDefinition = _definition
    override lazy val members: Seq[draco.TypeDefinition] = _members
    override lazy val typeDefinition: TypeDefinition = Latent.typeDefinition
  }

  lazy val Null: Latent = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition],
    _members = Seq.empty
  )


}
