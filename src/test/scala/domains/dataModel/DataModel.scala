package domains.dataModel

import draco._

trait DataModel extends DomainInstance

/** Common abstractions — DataModel rules match on these */
trait PartOne extends DataModel { val number: Int }
trait PartTwo extends DataModel { val text: String }
trait Assembled extends DataModel { val number: Int; val text: String }

object DataModel {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("DataModel", _namePackage = Seq ("domains", "dataModel")))
  lazy val typeInstance: Type[DataModel] = Type[DataModel] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[DataModel] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
