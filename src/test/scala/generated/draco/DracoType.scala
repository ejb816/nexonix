
package generated.draco

import draco._

trait DracoType  {
  val typeDefinition: TypeDefinition
}

object DracoType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("DracoType", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[DracoType] = Type[DracoType] (typeDefinition)
}
