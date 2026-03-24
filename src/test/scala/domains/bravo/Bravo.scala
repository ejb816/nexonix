package domains.bravo

import domains.dataModel._
import draco._

trait Bravo extends DataModel

trait BravoResult extends Bravo with Assembled

object Bravo {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Bravo",
      _namePackage = Seq ("domains", "bravo")
    ),
    _derivation = Seq (
      TypeName ("DataModel", _namePackage = Seq ("domains", "dataModel"))
    )
  )
  lazy val typeInstance: Type[Bravo] = Type[Bravo] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Bravo] {
    override lazy val domainDefinition: TypeDefinition = TypeDefinition (
      typeDefinition.typeName,
      _elementTypeNames = Seq ("BravoResult")
    )
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }

  def result(_number: Int, _text: String): BravoResult = new BravoResult {
    override val number: Int = _number
    override val text: String = _text
    override val typeDefinition: TypeDefinition = Bravo.typeDefinition
    override val typeInstance: DracoType = Bravo.typeInstance
    override val domainInstance: DomainType = Bravo.domainInstance
  }
}
