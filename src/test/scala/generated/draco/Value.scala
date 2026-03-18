
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Value  {
  val name: String
  val pathElements: Seq[String]
}

object Value extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Value", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Value] = Type[Value] (typeDefinition)

  implicit lazy val encoder: Encoder[Value] = Encoder.instance { x =>
    val fields = Seq(
      Some("name" -> x.name.asJson),
      Some("pathElements" -> x.pathElements.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Value] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[String]
      _pathElements <- cursor.downField("pathElements").as[Seq[String]]
    } yield Value (_name, _pathElements)
  }

  def apply (
    _name: String,
    _pathElements: Seq[String]
  ) : Value = new Value {
    override val name: String = _name
    override val pathElements: Seq[String] = _pathElements
    override lazy val typeInstance: DracoType = Value.typeInstance
    override lazy val typeDefinition: TypeDefinition = Value.typeDefinition
  }

  lazy val Null: Value = apply(
    _name = "",
    _pathElements = Seq.empty
  )


}
