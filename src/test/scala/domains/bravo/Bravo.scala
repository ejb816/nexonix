package domains.bravo

import domains.dataModel._
import draco._

trait Bravo extends DataModel

trait BravoResult extends Bravo with Assembled

object Bravo {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Bravo", _namePackage = Seq ("domains", "bravo")))
  lazy val domainType: Domain[Bravo] = Domain[Bravo] (typeDefinition)

  def result(_number: Int, _text: String): BravoResult = new BravoResult {
    override val number: Int = _number
    override val text: String = _text
  }
}
