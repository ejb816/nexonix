
package generated.draco

import draco._

trait DomainType extends DracoType {
  val domainDefinition: DomainDefinition
  val typeDictionary: TypeDictionary
}

object DomainType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("DomainType", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[DomainType] = Type[DomainType] (typeDefinition)
}
