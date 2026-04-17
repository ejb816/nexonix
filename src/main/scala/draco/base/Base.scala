package draco.base

import draco._

trait Base extends DomainInstance

object Base extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Base", _namePackage = Seq ("draco", "base")))
  lazy val typeInstance: Type[Base] = Type[Base] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Base] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
