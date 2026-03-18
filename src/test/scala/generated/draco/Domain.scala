
package generated.draco

import draco._

trait Domain[T] extends DomainType 

object Domain extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Domain", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val typeInstance: Type[Domain[_]] = Type[Domain[_]] (typeDefinition)
}
