
package generated.draco

import draco._

trait DomainDictionary extends Dictionary[DomainType, TypeDictionary] 

object DomainDictionary extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("DomainDictionary", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[DomainDictionary] = Type[DomainDictionary] (typeDefinition)

  def apply (
    _domains: Seq[DomainType] = Seq()
  ) : DomainDictionary = new DomainDictionary {
    override val domains: Seq[DomainType] = _domains
    override lazy val typeInstance: DracoType = DomainDictionary.typeInstance
    override lazy val typeDefinition: TypeDefinition = DomainDictionary.typeDefinition
  }

  lazy val Null: DomainDictionary = apply(
    _domains = Seq()
  )


}
