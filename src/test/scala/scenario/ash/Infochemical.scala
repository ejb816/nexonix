package scenario.ash

import draco._
import scenario._

trait Infochemical extends DracoType

object Infochemical extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Infochemical", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[Infochemical] = Type[Infochemical] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)
}
