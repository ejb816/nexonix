package domains.delta

import domains.dataModel.DataModel
import draco._

trait Delta extends DataModel

object Delta {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Delta",
      _namePackage = Seq ("domains", "delta")
    ),
    _derivation = Seq (
      TypeName ("DataModel", _namePackage = Seq ("domains", "dataModel"))
    )
  )
  lazy val typeInstance: Type[Delta] = Type[Delta] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Delta] {
    override lazy val domainDefinition: TypeDefinition = TypeDefinition (
      typeDefinition.typeName
    )
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
