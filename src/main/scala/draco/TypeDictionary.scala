package draco

trait TypeDictionary extends Dictionary[TypeName,TypeDefinition] {
  val  elementTypes: Seq[TypeDefinition]
}

object TypeDictionary extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "TypeDictionary",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("Dictionary[TypeName,TypeDefinition]", _namePackage = Seq ("org", "nexonix", "domains"))
    ),
    _elements = Seq (
      Fixed ("elementTypes", "Seq[TypeDefinition]")
    ),
    _factory = Factory (
      "TypeDictionary",
      _parameters = Seq (
        Parameter ("domainName", "DomainName", "")
      )
    )
  )
  lazy val typeInstance: Type[TypeDictionary] = Type[TypeDictionary] (typeDefinition)

  def apply (_domainName: DomainName) : TypeDictionary = new TypeDictionary {
    override val elementTypes: Seq[TypeDefinition] = _domainName.elementTypeNames.map (name =>
      TypeDefinition (TypeName (name, _domainName.typeName.parent)))
    override val kvMap: Map[TypeName, TypeDefinition]  = elementTypes.map (td => (td.typeName, td)).toMap
  }
  lazy val Null: TypeDictionary = TypeDictionary(DomainName.Null)
}