package scenario.birch

import draco._
import scenario._

trait AlarmSignal extends BirchSap {
  val infochemical: BirchJasmonate
}

object AlarmSignal extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("AlarmSignal", _namePackage = Seq ("scenario", "birch")))
  lazy val dracoType: Type[AlarmSignal] = Type[AlarmSignal] (typeDefinition)
  lazy val domainType: Domain[Birch] = Domain[Birch] (typeDefinition)
}
