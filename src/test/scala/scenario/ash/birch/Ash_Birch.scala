package scenario.ash.birch

import draco._
import scenario.ash._
import scenario._
import scenario.forest._
import scenario.birch._

trait Ash_Birch extends DracoType

object Ash_Birch extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Ash_Birch", _namePackage = Seq ("scenario", "ash", "birch")))
  lazy val dracoType: Type[Ash_Birch] = Type[Ash_Birch] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Potency", "Marker")

  lazy val domainType: Domain[Ash_Birch] = Domain[Ash_Birch] (typeDefinition)
}
