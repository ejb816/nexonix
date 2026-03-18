
package generated.draco

import draco._

trait TypeDictionary extends Dictionary[TypeName, TypeDefinition] {
  val elementTypes: Seq[TypeDefinition]
}

object TypeDictionary extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("TypeDictionary", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[TypeDictionary] = Type[TypeDictionary] (typeDefinition)

  def apply (
    _domainDefinition: DomainDefinition
  ) : TypeDictionary = new TypeDictionary {
    override val domainDefinition: DomainDefinition = _domainDefinition
    override lazy val typeInstance: DracoType = TypeDictionary.typeInstance
    override lazy val typeDefinition: TypeDefinition = TypeDictionary.typeDefinition
  }

  lazy val Null: TypeDictionary = apply(
    _domainDefinition = null.asInstanceOf[DomainDefinition]
  )


}
