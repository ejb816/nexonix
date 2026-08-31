package scenario.ash

import draco._
import scenario._

trait AshSap extends Ash

object AshSap extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AshSap", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[AshSap] = Type[AshSap] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)
}
