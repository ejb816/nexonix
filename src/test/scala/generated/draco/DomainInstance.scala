
package generated.draco

import draco._

trait DomainInstance extends TypeInstance {
  val domainInstance: DomainType
}

object DomainInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("DomainInstance", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[DomainInstance] = Type[DomainInstance] (typeDefinition)
}
