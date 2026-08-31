package scenario.ash.birch

import draco._
import scenario.ash._
import scenario._
import scenario.birch._

trait Potency extends scenario.birch.Micromolar

object Potency extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Potency", _namePackage = Seq ("scenario", "ash", "birch")))
  lazy val dracoType: Type[Potency] = Type[Potency] (typeDefinition)
  lazy val domainType: Domain[Ash_Birch] = Domain[Ash_Birch] (typeDefinition)

  def apply (
    _potency: scenario.ash.Micromolar
  ) : Potency = new Potency {
    override lazy val value: Double = _potency.value * 0.62
    override lazy val typeDefinition: TypeDefinition = Potency.typeDefinition
  }

  lazy val Null: Potency = apply(
    _potency = null.asInstanceOf[scenario.ash.Micromolar]
  )


}
