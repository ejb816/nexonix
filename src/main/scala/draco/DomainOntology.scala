package draco

trait DomainOntology extends Dictionary[DomainType, TypeDictionary] {
  def defines(_typeName: => TypeName): Boolean = {
    lazy val typeName: TypeName = _typeName
    kvMap.exists(entry => entry._1.typeDefinition.typeName.namePath == typeName.namePath || entry._2.elementTypes.exists(td => td.typeName.namePath == typeName.namePath))
  }
}

object DomainOntology extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainOntology", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DomainOntology] = Type[DomainOntology] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _domains: => Seq[DomainType] = Seq.empty
  ) : DomainOntology = new DomainOntology {
    override lazy val kvMap: Map[DomainType, TypeDictionary] = _domains.map(domain => (domain, domain.typeDictionary)).toMap
    override lazy val typeDefinition: TypeDefinition = DomainOntology.typeDefinition
  }

  lazy val Null: DomainOntology = apply()


}
