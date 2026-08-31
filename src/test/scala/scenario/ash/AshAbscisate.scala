package scenario.ash

import draco._
import scenario._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait AshAbscisate extends Infochemical {
  val potency: Micromolar
  val compound: Compound
}

object AshAbscisate extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AshAbscisate", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[AshAbscisate] = Type[AshAbscisate] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)

  implicit lazy val encoder: Encoder[AshAbscisate] = Encoder.instance { x =>
    val fields = Seq(
      Some("potency" -> x.potency.asJson),
      Some("compound" -> x.compound.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[AshAbscisate] = Decoder.instance { cursor =>
    for {
      _potency <- cursor.downField("potency").as[Micromolar]
      _compound <- cursor.downField("compound").as[Compound]
    } yield AshAbscisate (_potency, _compound)
  }

  def apply (
    _potency: Micromolar,
    _compound: Compound
  ) : AshAbscisate = new AshAbscisate {
    override lazy val potency: Micromolar = _potency
    override lazy val compound: Compound = _compound
    override lazy val typeDefinition: TypeDefinition = AshAbscisate.typeDefinition
  }

  lazy val Null: AshAbscisate = apply(
    _potency = null.asInstanceOf[Micromolar],
    _compound = null.asInstanceOf[Compound]
  )


}
