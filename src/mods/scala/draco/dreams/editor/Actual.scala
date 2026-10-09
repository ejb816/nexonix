package draco.dreams.editor

import draco.dreams._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Actual extends DracoType {
  val definition: draco.TypeDefinition
  val members: Seq[draco.TypeDefinition]
  val problems: Seq[draco.Problem]
  def accepted: Boolean = problems.isEmpty
  def domain: draco.DomainType = draco.Domain(definition, members)
}

object Actual extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Actual", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[Actual] = Type[Actual] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  implicit lazy val encoder: Encoder[Actual] = Encoder.instance { x =>
    val fields = Seq(
      Some("definition" -> x.definition.asJson),
      if (x.members.nonEmpty) Some("members" -> x.members.asJson) else None,
      if (x.problems.nonEmpty) Some("problems" -> x.problems.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Actual] = Decoder.instance { cursor =>
    for {
      _definition <- cursor.downField("definition").as[draco.TypeDefinition]
      _members <- cursor.downField("members").as[Option[Seq[draco.TypeDefinition]]].map(_.getOrElse(Seq.empty))
      _problems <- cursor.downField("problems").as[Option[Seq[draco.Problem]]].map(_.getOrElse(Seq.empty))
    } yield Actual (_definition, _members, _problems)
  }

  def apply (
    _definition: => draco.TypeDefinition,
    _members: => Seq[draco.TypeDefinition] = Seq.empty,
    _problems: => Seq[draco.Problem] = Seq.empty
  ) : Actual = new Actual {
    override lazy val definition: draco.TypeDefinition = _definition
    override lazy val members: Seq[draco.TypeDefinition] = _members
    override lazy val problems: Seq[draco.Problem] = _problems
    override lazy val typeDefinition: TypeDefinition = Actual.typeDefinition
  }

  lazy val Null: Actual = apply(
    _definition = null.asInstanceOf[draco.TypeDefinition],
    _members = Seq.empty,
    _problems = Seq.empty
  )


}
