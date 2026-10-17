package domains.terrestrial

import draco._
import domains._
import draco.format.dracon._

trait LocationReport extends Terrestrial with Dracon

object LocationReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("LocationReport", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[LocationReport] = Type[LocationReport] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)
}
