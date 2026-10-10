package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Derivation extends DracoType {
  val child: TypeName
  val parent: TypeName
  val declared: Boolean
}

object Derivation extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Derivation", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Derivation] = Type[Derivation] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[Derivation] = Encoder.instance { x =>
    val fields = Seq(
      Some("child" -> x.child.asJson),
      Some("parent" -> x.parent.asJson),
      if (x.declared) Some("declared" -> x.declared.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Derivation] = Decoder.instance { cursor =>
    for {
      _child <- cursor.downField("child").as[TypeName]
      _parent <- cursor.downField("parent").as[TypeName]
      _declared <- cursor.downField("declared").as[Option[Boolean]].map(_.getOrElse(true))
    } yield Derivation (_child, _parent, _declared)
  }

  def apply (
    _child: => TypeName,
    _parent: => TypeName,
    _declared: => Boolean = true
  ) : Derivation = new Derivation {
    override lazy val child: TypeName = _child
    override lazy val parent: TypeName = _parent
    override lazy val declared: Boolean = _declared
    override lazy val typeDefinition: TypeDefinition = Derivation.typeDefinition
  }

  lazy val Null: Derivation = apply(
    _child = null.asInstanceOf[TypeName],
    _parent = null.asInstanceOf[TypeName],
    _declared = true
  )


}
