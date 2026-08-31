package scenario.ash

import draco._
import scenario._
import scenario.forest._

trait Ash extends DracoType

object Ash extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Ash", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[Ash] = Type[Ash] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("AshSap", "AlarmSignal", "DroughtCue", "Infochemical", "AshJasmonate", "AshAbscisate", "Micromolar", "Compound", "RootInterface")

  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)
}
