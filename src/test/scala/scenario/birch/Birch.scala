package scenario.birch

import draco._
import scenario._
import scenario.forest._

trait Birch extends DracoType

object Birch extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Birch", _namePackage = Seq ("scenario", "birch")))
  lazy val dracoType: Type[Birch] = Type[Birch] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("BirchSap", "AlarmSignal", "Infochemical", "BirchJasmonate", "Micromolar", "Compound")

  lazy val domainType: Domain[Birch] = Domain[Birch] (typeDefinition)
}
