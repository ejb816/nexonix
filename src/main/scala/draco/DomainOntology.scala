package draco

trait DomainOntology extends Dictionary[DomainType, TypeDictionary] {
  val derivations: Seq[Derivation]
  def owns(_domain: => DomainType, _typeName: => TypeName): Boolean = {
    lazy val domain: DomainType = _domain
    lazy val typeName: TypeName = _typeName
    domain.typeDefinition.typeName.namePath == typeName.namePath || domain.typeDictionary.elementTypes.exists(td => td.typeName.namePath == typeName.namePath)
  }
  def defines(_typeName: => TypeName): Boolean = {
    lazy val typeName: TypeName = _typeName
    kvMap.keys.exists(domain => owns(domain, typeName))
  }
  def dependencies(_domain: => DomainType): Seq[DomainType] = {
    lazy val domain: DomainType = _domain
    kvMap.keys.filter(other => other.typeDefinition.typeName != domain.typeDefinition.typeName && derivations.exists(edge => owns(domain, edge.child) && owns(other, edge.parent))).toSeq
  }
  def including(_domain: => DomainType): DomainOntology = {
    lazy val domain: DomainType = _domain
    DomainOntology(kvMap.keys.toSeq ++ Seq(domain))
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
    override lazy val derivations: Seq[Derivation] = _domains.flatMap(domain => domain.typeDictionary.elementTypes.prepended(domain.typeDefinition).flatMap(td => DracoAspect.parents(td.dracoAspect).map(parent => Derivation(td.typeName, parent))))
    override lazy val typeDefinition: TypeDefinition = DomainOntology.typeDefinition
  }

  lazy val Null: DomainOntology = apply()


}
