package domains.charlie

import domains.dataModel.DataModel
import draco._

trait Charlie extends DataModel

object Charlie {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Charlie", _namePackage = Seq ("domains", "charlie")))
  lazy val domainType: Domain[Charlie] = Domain[Charlie] (typeDefinition)
}
