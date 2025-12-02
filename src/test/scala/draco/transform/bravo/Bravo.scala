package draco.transform.bravo

import draco.transform.dataModel.DataModel
import draco.{DomainDictionary, DomainName, DomainType, TypeDefinition, TypeDictionary, TypeName}

trait Bravo extends DataModel {}

object Bravo {
  val bravo: Bravo = new Bravo {
    override val domainName: DomainName = DomainName (
    TypeName (
      _name = "Bravo",
      _namePackage = Seq ("draco","transform", "bravo")
    )
  )
    override val typeDefinition: TypeDefinition = TypeDefinition (domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = Seq ()
    override val domainDictionary: DomainDictionary = DomainDictionary (Seq (DataModel.dataModel))
  }
}