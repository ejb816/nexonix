package domain.datamodel

import draco.{DomainDictionary, DomainDictionaryTest, DomainName, DomainType, TypeName}

trait DataModelService {
  val domainPackage: DomainType = DomainType (DomainName (TypeName ("Domain")), Seq ("DataModel", "Alpha", "Bravo", "Charlie", "Delta"))
  val dataModel: DomainType
  val alpha: DomainType
  val bravo: DomainType
  val charlie: DomainType
  val delta: DomainType
  val dataModelDictionary : DomainDictionary
}

object DataModelService {
  def apply () : DataModelService = new DataModelService {
    override val dataModel: DomainType = DomainType (
      DomainName (
        TypeName (
          domainPackage.subDomainNames.head,
          _parent = domainPackage.typeDefinition.typeName.fullName)),
      domainPackage.subDomainNames.tail)
    override val alpha: DomainType = DomainType (
      DomainName (
        TypeName (
          domainPackage.subDomainNames(1),
          _parent = domainPackage.typeDefinition.typeName.fullName)))
    override val bravo: DomainType = DomainType (
      DomainName (
        TypeName (
          domainPackage.subDomainNames(2),
          _parent = domainPackage.typeDefinition.typeName.fullName)))
    override val charlie: DomainType = DomainType (
      DomainName (
        TypeName (
          domainPackage.subDomainNames(3),
          _parent = domainPackage.typeDefinition.typeName.fullName)))
    override val delta: DomainType = DomainType (
      DomainName (
        TypeName (
          domainPackage.subDomainNames(4),
          _parent =domainPackage.typeDefinition.typeName.fullName)))
    override val dataModelDictionary: DomainDictionary = DomainDictionary(Seq (domainPackage, dataModel, alpha, bravo, charlie, delta))
  }
}
