package scenario.forest

import draco._
import scenario._

trait Forest extends DracoType

object Forest extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Forest", _namePackage = Seq ("scenario", "forest")))
  lazy val dracoType: Type[Forest] = Type[Forest] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("AshBirchAlarm", "BirchAlarmReceived")

  lazy val domainType: Domain[Forest] = Domain[Forest] (typeDefinition)
}
