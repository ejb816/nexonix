package draco.base

import draco.{DomainName, Draco, TypeDefinition, TypeDictionary, TypeName}

trait Base extends Draco {}

object Base {
  private val elementTypeNames: Seq[String] = Seq ()
  def apply (_domainName: DomainName = DomainName ( TypeName ("Base", "draco.base"), elementTypeNames)) : Base = new Base {
    override val typeDefinition: TypeDefinition = TypeDefinition.load(_domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (_domainName)
    override val subDomainNames: Seq[String] = Seq[String] ()
  }
}
