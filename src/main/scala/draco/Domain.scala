package draco

trait Domain extends Draco with DomainName {
  val typeDefinition: TypeDefinition
  val typeDictionary: TypeDictionary
  val domainNames: Seq[String]
}

object Domain {
  def apply (_domainName: DomainName, _domainNames: Seq[String] = Seq ()) : Domain = new Domain {
    override val typeName: TypeName = _domainName.typeName
    override val elementNames: Seq[String] = _domainName.elementNames
    override val typeDefinition: TypeDefinition = TypeDefinition.load(typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary(_domainName)
    override val domainNames: Seq[String] = _domainNames
  }
}

