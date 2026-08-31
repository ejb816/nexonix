package scenario.ash.birch

import draco._
import scenario.ash._
import scenario._
import scenario.birch._

trait Marker extends scenario.birch.Compound

object Marker extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Marker", _namePackage = Seq ("scenario", "ash", "birch")))
  lazy val dracoType: Type[Marker] = Type[Marker] (typeDefinition)
  lazy val domainType: Domain[Ash_Birch] = Domain[Ash_Birch] (typeDefinition)

  def apply (
    _compound: scenario.ash.Compound
  ) : Marker = new Marker {
    override lazy val value: String = _compound.value
    override lazy val typeDefinition: TypeDefinition = Marker.typeDefinition
  }

  lazy val Null: Marker = apply(
    _compound = null.asInstanceOf[scenario.ash.Compound]
  )


}
