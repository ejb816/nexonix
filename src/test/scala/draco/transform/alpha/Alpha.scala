package draco.transform.alpha

import draco.transform.dataModel.DataModel
import draco.{DomainDictionary, DomainName, DomainType, TypeDefinition, TypeDictionary, TypeName}

trait Alpha extends DataModel {}

object Alpha {
  val alpha: Alpha = new Alpha {
    override val domainName: DomainName = DomainName (
      TypeName (
        _name = "Alpha",
        _namePackage = Seq ("draco", "transform", "alpha")
      )
    )
    override val typeDefinition: TypeDefinition = TypeDefinition (domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = Seq ()
    override val domainDictionary: DomainDictionary = DomainDictionary (Seq (DataModel.dataModel))
  }
}