package domains.charlie

import domains.dataModel.DataModel
import draco._

trait Charlie extends DataModel

object Charlie {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Charlie", _namePackage = Seq ("domains", "charlie")))
  lazy val typeInstance: Type[Charlie] = Type[Charlie] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Charlie] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
