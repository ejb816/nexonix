
package generated.draco

import draco._

trait Dictionary[K, V] extends Map[K, V] {
  val kvMap: Map[K, V]
}

object Dictionary extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Dictionary", _namePackage = Seq("draco"), _typeParameters = Seq("K", "V")))
  lazy val typeInstance: Type[Dictionary[_, _]] = Type[Dictionary[_, _]] (typeDefinition)
}
