package draco

trait Draco extends DomainInstance {
  val superDomain: DomainType = DomainType.Null
}

object Draco extends App with DomainInstance {
  lazy val typeInstance: Type[Draco] = new Type[Draco] {
    override val typeDefinition: TypeDefinition = TypeDefinition (
      _typeName = TypeName (
        _name = "Draco",
        _namePackage = Seq ("draco")
      ),
      _derivation = Seq (
        TypeName ("DomainInstance", _namePackage = Seq ("draco"))
      )
    )
  }

  lazy val domainInstance: DomainType = Domain[Draco] (
    _domainDefinition = DomainDefinition (
      _typeName = TypeName (
        "Draco"
      ),
      _elementTypeNames = Seq (
        "ActorBehavior",
        "ContentSink",
        "Dictionary",
        "Domain",
        "DomainDictionary",
        "DomainDefinition",
        "DomainElement",
        "DomainType",
        "Generator",
        "Main",
        "Primal",
        "Rule",
        "RuleActorBehavior",
        "RuleDefinition",
        "Service",
        "SourceContent",
        "Test",
        "TypeDefinition",
        "TypeDictionary",
        "TypeElement",
        "TypeName",
        "Value"
      )
    )
  )
  override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
}
