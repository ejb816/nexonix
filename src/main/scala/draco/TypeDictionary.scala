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
      TypeName ("Dictionary", _namePackage = Seq ("draco"), _typeParameters = Seq ("TypeName", "TypeDefinition"))
    ),
    _elements = Seq (
      Fixed ("elementTypes", "Seq[TypeDefinition]")
    ),
    _factory = Factory (
      "TypeDictionary",
      _parameters = Seq (
        Parameter ("domainDefinition", "TypeDefinition", "")
      )
    )
  )
  lazy val typeInstance: Type[TypeDictionary] = Type[TypeDictionary] (typeDefinition)

  def apply (_domainDefinition: TypeDefinition) : TypeDictionary = new TypeDictionary {
    override val elementTypes: Seq[TypeDefinition] = _domainDefinition.elementTypeNames.map (name =>
      TypeDefinition (TypeName (name, _namePackage = _domainDefinition.typeName.namePackage)))
    override val kvMap: Map[TypeName, TypeDefinition]  = elementTypes.map (td => (td.typeName, td)).toMap
  }
  lazy val Null: TypeDictionary = TypeDictionary(TypeDefinition.Null)
}
