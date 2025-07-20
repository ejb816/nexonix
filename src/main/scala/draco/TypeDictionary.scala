package draco

trait TypeDictionary extends Dictionary[TypeName,TypeDefinition] {
  val rootType: TypeDefinition = TypeDefinition.Null
  val elementTypes: Seq[TypeDefinition] = Seq()
}

object TypeDictionary extends App {
  val Null = new TypeDictionary {
    override val kvMap: Map[TypeName, TypeDefinition] = Map()
  }
  def apply (rootTypeName: TypeName, elementNames: Seq[String]) : TypeDictionary = new TypeDictionary {
    override val rootType: TypeDefinition = TypeDefinition.load(rootTypeName)
    override val elementTypes: Seq[TypeDefinition] = Seq(rootType) ++ TypeDefinition.load(rootType.typeName, elementNames)
    override val kvMap: Map[TypeName, TypeDefinition] = elementTypes.map(td => (td.typeName, td)).toMap
  }
}