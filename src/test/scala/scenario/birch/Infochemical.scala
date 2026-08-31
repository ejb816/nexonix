package scenario.birch

import draco._
import scenario._

trait Infochemical extends DracoType

object Infochemical extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Infochemical", _namePackage = Seq ("scenario", "birch")))
  lazy val dracoType: Type[Infochemical] = Type[Infochemical] (typeDefinition)
  lazy val domainType: Domain[Birch] = Domain[Birch] (typeDefinition)
}
