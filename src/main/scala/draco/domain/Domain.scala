package draco.domain

import draco.{RootType, TypeDefinition, TypeDictionary, TypeName}

trait Domain extends RootType {
  val domainType: TypeDefinition
}

object Domain {
  def apply(rootType: RootType) : Domain = new Domain {
    override val typeName: TypeName = rootType.typeName
    override val elementNames: Seq[String] = rootType.elementNames
    override val typeDictionary: TypeDictionary = TypeDictionary(typeName, elementNames)
    override val domainType: TypeDefinition = typeDictionary(typeName)
  }
}