package scenario.ash

import draco._
import scenario._

trait DroughtCue extends AshSap {
  val infochemical: AshAbscisate
}

object DroughtCue extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DroughtCue", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[DroughtCue] = Type[DroughtCue] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)
}
