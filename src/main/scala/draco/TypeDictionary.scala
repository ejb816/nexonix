package draco

trait TypeDictionary extends Dictionary[TypeName, TypeDefinition] {
  val elementTypes: Seq[TypeDefinition]
}

object TypeDictionary extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeDictionary", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeDictionary] = Type[TypeDictionary] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _domainDefinition: => TypeDefinition,
    _members: => Seq[TypeDefinition] = Seq.empty
  ) : TypeDictionary = new TypeDictionary {
    lazy val domainDefinition: TypeDefinition = _domainDefinition
    lazy val supplied: Map[String, TypeDefinition] = _members.filter(td => td.typeName.namePackage == domainDefinition.typeName.namePackage).map(td => (td.typeName.name, td)).toMap
    override lazy val elementTypes: Seq[TypeDefinition] = domainDefinition.domainAspect.elementTypeNames.map(name => supplied.getOrElse(name, TypeDefinition(TypeName(name, _namePackage = domainDefinition.typeName.namePackage))))
    override lazy val kvMap: Map[TypeName, TypeDefinition] = elementTypes.map(td => (td.typeName, td)).toMap
    override lazy val typeDefinition: TypeDefinition = TypeDictionary.typeDefinition
  }

  lazy val Null: TypeDictionary = apply(
    _domainDefinition = null.asInstanceOf[TypeDefinition],
    _members = Seq.empty
  )


}
