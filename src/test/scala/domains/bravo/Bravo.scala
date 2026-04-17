package domains.bravo

import domains.dataModel._
import draco._

trait Bravo extends DataModel

trait BravoResult extends Bravo with Assembled

object Bravo {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Bravo", _namePackage = Seq ("domains", "bravo")))
  lazy val typeInstance: Type[Bravo] = Type[Bravo] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Bravo] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
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
