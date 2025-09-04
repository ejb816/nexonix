package org.nexonix.actor

import draco.{Domain, DomainDictionary, DomainName, TypeName}

trait DataModelService {
  val domainPackage: Domain = Domain (DomainName (TypeName ("Domain")), Seq ("DataModel", "Alpha", "Bravo", "Charlie", "Delta"))
  val dataModel: Domain
  val alpha: Domain
  val bravo: Domain
  val charlie: Domain
  val delta: Domain
  val dataModelDictionary : DomainDictionary
}

object DataModelService extends App {
  def apply (_parentPackage: Seq[String] = Seq ()) : DataModelService = new DataModelService {
    override val dataModel: Domain = Domain (
      DomainName (
        TypeName (
          domainPackage.domainNames.head,
          _namePackage = _parentPackage)),
      domainPackage.domainNames.tail)
    override val alpha: Domain = Domain (
      DomainName (
        TypeName (
          domainPackage.domainNames(1),
          _namePackage = _parentPackage)))
    override val bravo: Domain = Domain (
      DomainName (
        TypeName (
          domainPackage.domainNames(2),
          _namePackage = _parentPackage)))
    override val charlie: Domain = Domain (
      DomainName (
        TypeName (
          domainPackage.domainNames(3),
          _namePackage = _parentPackage)))
    override val delta: Domain = Domain (
      DomainName (
        TypeName (
          domainPackage.domainNames(4),
          _namePackage = _parentPackage)))
    override val dataModelDictionary: DomainDictionary = DomainDictionary(Seq (domainPackage, dataModel, alpha, bravo, charlie, delta))
  }
}
