package domains.dataModel

import draco._

trait DataModel extends Extensible

/** Common abstractions — DataModel rules match on these */
trait PartOne extends DataModel { val number: Int }
trait PartTwo extends DataModel { val text: String }
trait Assembled extends DataModel { val number: Int; val text: String }

object DataModel {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("DataModel", _namePackage = Seq ("domains", "dataModel")))
  lazy val domainType: Domain[DataModel] = Domain[DataModel] (typeDefinition)
}
