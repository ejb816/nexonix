
package generated.draco

import draco._

trait TypeInstance extends DracoType {
  val typeInstance: DracoType
}

object TypeInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("TypeInstance", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[TypeInstance] = Type[TypeInstance] (typeDefinition)
}
