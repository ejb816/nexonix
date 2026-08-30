package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Type[T] extends DracoType

object Type extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Type", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Type[_]] = Type[Type[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[Type[_]] = Encoder.instance { x =>
    val fields = Seq(
      Some("typeDefinition" -> x.typeDefinition.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Type[_]] = Decoder.instance { cursor =>
    for {
      _typeDefinition <- cursor.downField("typeDefinition").as[TypeDefinition]
    } yield Type (_typeDefinition)
  }

  def apply[T] (
    _typeDefinition: TypeDefinition
  ) : Type[T] = new Type[T] {
    override lazy val typeDefinition: TypeDefinition = _typeDefinition
  }

  lazy val Null: Type[_] = apply[Nothing](
    _typeDefinition = null.asInstanceOf[TypeDefinition]
  )


}
