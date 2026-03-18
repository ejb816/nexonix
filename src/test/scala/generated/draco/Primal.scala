
package generated.draco

import draco._

trait Primal[T] extends DracoType {
  val value: T
}

object Primal extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Primal", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val typeInstance: Type[Primal[_]] = Type[Primal[_]] (typeDefinition)
}
