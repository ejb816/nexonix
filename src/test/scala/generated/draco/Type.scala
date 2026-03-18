
package generated.draco

import draco._

trait Type[T] extends DracoType 

object Type extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Type", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val typeInstance: Type[Type[_]] = Type[Type[_]] (typeDefinition)

  def apply[T] (
    __typeDefinition: TypeDefinition
  ) : Type[T] = new Type[T] {
    override val _typeDefinition: TypeDefinition = __typeDefinition
    override lazy val typeInstance: DracoType = Type.typeInstance
    override lazy val typeDefinition: TypeDefinition = Type.typeDefinition
  }

  lazy val Null: Type[_] = apply[Nothing](
    __typeDefinition = null.asInstanceOf[TypeDefinition]
  )


}
