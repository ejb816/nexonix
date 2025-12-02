package draco.transform.delta

import draco.transform.dataModel.DataModel
import draco.{DomainDictionary, DomainName, DomainType, TypeDefinition, TypeDictionary, TypeName}

trait Delta extends DataModel {}

object Delta {
  val delta: Delta = new Delta {
    override val domainName: DomainName = DomainName (
      TypeName (
        _name = "Delta",
        _namePackage = Seq ("draco", "transform", "delta")
      )
    )
    override val typeDefinition: TypeDefinition = TypeDefinition (domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = Seq ()
    override val domainDictionary: DomainDictionary = DomainDictionary (Seq (DataModel.dataModel))

  }
}