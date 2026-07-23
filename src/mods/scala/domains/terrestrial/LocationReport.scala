package domains.terrestrial

import draco._
import domains._
import draco.format.json._

trait LocationReport extends Terrestrial with JSON

object LocationReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("LocationReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[LocationReport] = Type[LocationReport] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)
}
