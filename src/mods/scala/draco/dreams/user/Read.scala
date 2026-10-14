package draco.dreams.user

import draco.dreams._
import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Read extends Request {
  val domain: draco.TypeName
}

object Read extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Read", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Read] = Type[Read] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)

  implicit lazy val encoder: Encoder[Read] = Encoder.instance { x =>
    val fields = Seq(
      Some("domain" -> x.domain.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Read] = Decoder.instance { cursor =>
    for {
      _domain <- cursor.downField("domain").as[draco.TypeName]
    } yield Read (_domain)
  }

  def apply (
    _domain: => draco.TypeName
  ) : Read = new Read {
    override lazy val domain: draco.TypeName = _domain
    override lazy val typeDefinition: TypeDefinition = Read.typeDefinition
  }

  lazy val Null: Read = apply(
    _domain = null.asInstanceOf[draco.TypeName]
  )


}
