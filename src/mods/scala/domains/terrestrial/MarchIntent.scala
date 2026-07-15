package domains.terrestrial

import draco._
import domains._
import draco.format.json._

trait MarchIntent extends Terrestrial with JSON

object MarchIntent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("MarchIntent", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[MarchIntent] = Type[MarchIntent] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)
}
