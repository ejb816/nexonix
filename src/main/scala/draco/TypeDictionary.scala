package draco

trait TypeDictionary extends Dictionary[TypeName,TypeDefinition] {
  val elementTypes: Seq[TypeDefinition]
}

object TypeDictionary extends App {
  lazy val Null: TypeDictionary = TypeDictionary(DomainName.Null)
  def apply (_domainName: DomainName) : TypeDictionary = new TypeDictionary {
    override val elementTypes: Seq[TypeDefinition] = _domainName.elementTypeNames.map (name =>
      TypeDefinition (TypeName (name, _domainName.typeName.parent)))
    override val kvMap: Map[TypeName, TypeDefinition]  = elementTypes.map (td => (td.typeName, td)).toMap
  }
}