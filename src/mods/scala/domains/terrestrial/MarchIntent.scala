package domains.terrestrial

import draco._
import domains._
import draco.format.dracon._

trait MarchIntent extends Terrestrial with Dracon

object MarchIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("MarchIntent", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[MarchIntent] = Type[MarchIntent] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)
}
