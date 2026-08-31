package scenario.ash

import draco._
import scenario._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait AshJasmonate extends Infochemical {
  val potency: Micromolar
  val compound: Compound
}

object AshJasmonate extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AshJasmonate", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[AshJasmonate] = Type[AshJasmonate] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)

  implicit lazy val encoder: Encoder[AshJasmonate] = Encoder.instance { x =>
    val fields = Seq(
      Some("potency" -> x.potency.asJson),
      Some("compound" -> x.compound.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[AshJasmonate] = Decoder.instance { cursor =>
    for {
      _potency <- cursor.downField("potency").as[Micromolar]
      _compound <- cursor.downField("compound").as[Compound]
    } yield AshJasmonate (_potency, _compound)
  }

  def apply (
    _potency: Micromolar,
    _compound: Compound
  ) : AshJasmonate = new AshJasmonate {
    override lazy val potency: Micromolar = _potency
    override lazy val compound: Compound = _compound
    override lazy val typeDefinition: TypeDefinition = AshJasmonate.typeDefinition
  }

  lazy val Null: AshJasmonate = apply(
    _potency = null.asInstanceOf[Micromolar],
    _compound = null.asInstanceOf[Compound]
  )


}
