package draco.transform.charlie

import draco.transform.dataModel.DataModel
import draco.{DomainDictionary, DomainName, DomainType, TypeDefinition, TypeDictionary, TypeName}

trait Charlie extends DataModel {}

object Charlie {
  val charlie: Charlie = new Charlie {
    override val domainName: DomainName = DomainName (
      TypeName (
        _name = "Charlie",
        _namePackage = Seq ("draco", "transform", "charlie")
      )
    )
    override val typeDefinition: TypeDefinition = TypeDefinition (domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = Seq ()
    override val domainDictionary: DomainDictionary = DomainDictionary (Seq (DataModel.dataModel))
  }
}