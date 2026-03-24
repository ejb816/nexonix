package draco

trait Draco extends DomainInstance {
  val superDomain: DomainType = DomainType.Null
}

object Draco extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Draco",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("DomainInstance", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[Draco] = Type[Draco] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Draco] {
    override lazy val domainDefinition: TypeDefinition = TypeDefinition (
      typeDefinition.typeName,
      _elementTypeNames = Seq (
        "Actor",
        "ContentSink",
        "Dictionary",
        "Domain",
        "DomainDictionary",
        "DomainType",
        "Extensible",
        "Generator",
        "Main",
        "Primal",
        "Rule",
        "SourceContent",
        "Specifically",
        "Test",
        "TypeDefinition",
        "TypeDictionary",
        "TypeElement",
        "TypeName",
        "Value"
      )
    )
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
