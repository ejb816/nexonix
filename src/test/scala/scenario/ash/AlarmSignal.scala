package scenario.ash

import draco._
import scenario._

trait AlarmSignal extends AshSap {
  val infochemical: AshJasmonate
}

object AlarmSignal extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AlarmSignal", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[AlarmSignal] = Type[AlarmSignal] (typeDefinition)
  lazy val domainType: Domain[Ash] = Domain[Ash] (typeDefinition)
}
