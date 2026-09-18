package draco

trait TypeDictionary extends Dictionary[TypeName, TypeDefinition] {
  val elementTypes: Seq[TypeDefinition]
}

object TypeDictionary extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeDictionary", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeDictionary] = Type[TypeDictionary] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _domainDefinition: => TypeDefinition
  ) : TypeDictionary = new TypeDictionary {
    lazy val domainDefinition: TypeDefinition = _domainDefinition
    override lazy val elementTypes: Seq[TypeDefinition] = domainDefinition.domainAspect.elementTypeNames.map(name => TypeDefinition(TypeName(name, _namePackage = domainDefinition.typeName.namePackage)))
    override lazy val kvMap: Map[TypeName, TypeDefinition] = elementTypes.map(td => (td.typeName, td)).toMap
    override lazy val typeDefinition: TypeDefinition = TypeDictionary.typeDefinition
  }

  lazy val Null: TypeDictionary = apply(
    _domainDefinition = null.asInstanceOf[TypeDefinition]
  )

}
