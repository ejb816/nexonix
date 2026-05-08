package domains.delta

import domains.dataModel.DataModel
import draco._

trait Delta extends DataModel

object Delta {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Delta", _namePackage = Seq ("domains", "delta")))
  lazy val domainType: Domain[Delta] = Domain[Delta] (typeDefinition)
}
