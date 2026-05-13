package draco

trait TypeDictionary extends Dictionary[TypeName, TypeDefinition] {
  val elementTypes: Seq[TypeDefinition]
}

object TypeDictionary extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("TypeDictionary", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeDictionary] = Type[TypeDictionary] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _domainDefinition: TypeDefinition
  ) : TypeDictionary = new TypeDictionary {
    override lazy val elementTypes: Seq[TypeDefinition] = _domainDefinition.domainAspect.elementTypeNames.map(name => TypeDefinition(TypeName(name, _namePackage = _domainDefinition.typeName.namePackage)))
    override lazy val kvMap: Map[TypeName, TypeDefinition] = elementTypes.map(td => (td.typeName, td)).toMap
    override lazy val typeDefinition: TypeDefinition = TypeDictionary.typeDefinition
  }

  lazy val Null: TypeDictionary = apply(
    _domainDefinition = null.asInstanceOf[TypeDefinition]
  )

}
