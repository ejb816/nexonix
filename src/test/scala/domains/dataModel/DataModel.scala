package domains.dataModel

import draco._

trait DataModel extends DomainInstance

object DataModel {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DataModel",
      _namePackage = Seq ("domains", "dataModel")
    )
  )
  lazy val typeInstance: Type[DataModel] = Type[DataModel] (typeDefinition)
  lazy val domainInstance: Domain[DataModel] = Domain[DataModel] (
    _domainDefinition = DomainDefinition (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
