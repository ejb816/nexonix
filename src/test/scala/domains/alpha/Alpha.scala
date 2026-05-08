package domains.alpha

import domains.dataModel._
import draco._

trait Alpha extends DataModel

trait AlphaOne extends Alpha with PartOne
trait AlphaTwo extends Alpha with PartTwo

object Alpha {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Alpha", _namePackage = Seq ("domains", "alpha")))
  lazy val domainType: Domain[Alpha] = Domain[Alpha] (typeDefinition)

  def one(_number: Int): AlphaOne = new AlphaOne {
    override val number: Int = _number
  }

  def two(_text: String): AlphaTwo = new AlphaTwo {
    override val text: String = _text
  }
}
