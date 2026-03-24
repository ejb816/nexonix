package domains.charlie

import domains.dataModel.DataModel
import draco._

trait Charlie extends DataModel

object Charlie {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Charlie",
      _namePackage = Seq ("domains", "charlie")
    ),
    _derivation = Seq (
      TypeName ("DataModel", _namePackage = Seq ("domains", "dataModel"))
    )
  )
  lazy val typeInstance: Type[Charlie] = Type[Charlie] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Charlie] {
    override lazy val domainDefinition: TypeDefinition = TypeDefinition (
      typeDefinition.typeName
    )
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
