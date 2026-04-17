package domains.delta

import domains.dataModel.DataModel
import draco._

trait Delta extends DataModel

object Delta {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Delta", _namePackage = Seq ("domains", "delta")))
  lazy val typeInstance: Type[Delta] = Type[Delta] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Delta] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
