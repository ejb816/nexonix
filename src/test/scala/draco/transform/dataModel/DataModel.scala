package draco.transform.dataModel

import draco._

trait DataModel extends DomainInstance

object DataModel {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DataModel",
      _namePackage = Seq ("draco", "transform", "dataModel")
    )
  )
  lazy val typeInstance: Type[DataModel] = Type[DataModel] (typeDefinition)
  lazy val domainInstance: Domain[DataModel] = Domain[DataModel] (
    _domainName = DomainName (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
