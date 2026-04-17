package domains.alpha

import domains.dataModel._
import draco._

trait Alpha extends DataModel

trait AlphaOne extends Alpha with PartOne
trait AlphaTwo extends Alpha with PartTwo

object Alpha {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Alpha", _namePackage = Seq ("domains", "alpha")))
  lazy val typeInstance: Type[Alpha] = Type[Alpha] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Alpha] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }

  def one(_number: Int): AlphaOne = new AlphaOne {
    override val number: Int = _number
    override val typeDefinition: TypeDefinition = Alpha.typeDefinition
    override val typeInstance: DracoType = Alpha.typeInstance
    override val domainInstance: DomainType = Alpha.domainInstance
  }

  def two(_text: String): AlphaTwo = new AlphaTwo {
    override val text: String = _text
    override val typeDefinition: TypeDefinition = Alpha.typeDefinition
    override val typeInstance: DracoType = Alpha.typeInstance
    override val domainInstance: DomainType = Alpha.domainInstance
  }
}
