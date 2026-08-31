package scenario.birch

import draco._
import scenario._

trait BirchSap extends Birch

object BirchSap extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("BirchSap", _namePackage = Seq ("scenario", "birch")))
  lazy val dracoType: Type[BirchSap] = Type[BirchSap] (typeDefinition)
  lazy val domainType: Domain[Birch] = Domain[Birch] (typeDefinition)
}
