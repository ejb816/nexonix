package draco.transform.dataModel

import draco.transform.Transform
import draco.transform.alpha.Alpha
import draco.transform.bravo.Bravo
import draco.transform.charlie.Charlie
import draco.transform.delta.Delta
import draco.{DomainDictionary, DomainName, DomainType, TypeDefinition, TypeDictionary, TypeName}

trait DataModel extends Transform {}

object DataModel {
  lazy val dataModel: DataModel = new DataModel {
    override val domainName: DomainName = DomainName (
      TypeName (
        _name = "DataModel",
        _namePackage = Seq ("draco", "transform", "dataModel")
      )
    )
    override val typeDefinition: TypeDefinition = TypeDefinition(domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary(domainName)
    override val domains: Seq[DomainType] = Seq (
      Alpha.alpha,
      Bravo.bravo,
      Charlie.charlie,
      Delta.delta
    )
    override val domainDictionary: DomainDictionary = DomainDictionary(Seq (this) ++ domains)
  }
}