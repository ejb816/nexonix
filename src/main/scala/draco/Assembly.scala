package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Assembly extends DracoType {
  val members: Seq[TypeName]
  val bindings: Seq[Binding]
  val entry: TypeName
}

object Assembly extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Assembly", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Assembly] = Type[Assembly] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[Assembly] = Encoder.instance { x =>
    val fields = Seq(
      if (x.members.nonEmpty) Some("members" -> x.members.asJson) else None,
      if (x.bindings.nonEmpty) Some("bindings" -> x.bindings.asJson) else None,
      if (x.entry.name.nonEmpty) Some("entry" -> x.entry.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Assembly] = Decoder.instance { cursor =>
    for {
      _members <- cursor.downField("members").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _bindings <- cursor.downField("bindings").as[Option[Seq[Binding]]].map(_.getOrElse(Seq.empty))
      _entry <- cursor.downField("entry").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
    } yield Assembly (_members, _bindings, _entry)
  }

  def apply (
    _members: Seq[TypeName] = Seq.empty,
    _bindings: Seq[Binding] = Seq.empty,
    _entry: TypeName = TypeName.Null
  ) : Assembly = new Assembly {
    override lazy val members: Seq[TypeName] = _members
    override lazy val bindings: Seq[Binding] = _bindings
    override lazy val entry: TypeName = _entry
    override lazy val typeDefinition: TypeDefinition = Assembly.typeDefinition
  }

  lazy val Null: Assembly = apply()

}
