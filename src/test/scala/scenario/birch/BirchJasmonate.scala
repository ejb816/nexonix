package scenario.birch

import draco._
import scenario._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait BirchJasmonate extends Infochemical {
  val potency: Micromolar
  val compound: Compound
}

object BirchJasmonate extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("BirchJasmonate", _namePackage = Seq ("scenario", "birch")))
  lazy val dracoType: Type[BirchJasmonate] = Type[BirchJasmonate] (typeDefinition)
  lazy val domainType: Domain[Birch] = Domain[Birch] (typeDefinition)

  implicit lazy val encoder: Encoder[BirchJasmonate] = Encoder.instance { x =>
    val fields = Seq(
      Some("potency" -> x.potency.asJson),
      Some("compound" -> x.compound.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[BirchJasmonate] = Decoder.instance { cursor =>
    for {
      _potency <- cursor.downField("potency").as[Micromolar]
      _compound <- cursor.downField("compound").as[Compound]
    } yield BirchJasmonate (_potency, _compound)
  }

  def apply (
    _potency: Micromolar,
    _compound: Compound
  ) : BirchJasmonate = new BirchJasmonate {
    override lazy val potency: Micromolar = _potency
    override lazy val compound: Compound = _compound
    override lazy val typeDefinition: TypeDefinition = BirchJasmonate.typeDefinition
  }

  lazy val Null: BirchJasmonate = apply(
    _potency = null.asInstanceOf[Micromolar],
    _compound = null.asInstanceOf[Compound]
  )


}
