package draco

trait DomainDictionary extends Dictionary[DomainType, TypeDictionary]

object DomainDictionary extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainDictionary", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DomainDictionary] = Type[DomainDictionary] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _domains: Seq[DomainType] = Seq()
  ) : DomainDictionary = new DomainDictionary {
    override lazy val kvMap: Map[DomainType, TypeDictionary] = _domains.map(domain => (domain, domain.typeDictionary)).toMap
    override lazy val typeDefinition: TypeDefinition = DomainDictionary.typeDefinition
  }

  lazy val Null: DomainDictionary = apply()

}
