package domains.ethereal

import draco._
import domains._
import draco.format.json._

trait EphemerisReport extends Ethereal with JSON

object EphemerisReport extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("EphemerisReport", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[EphemerisReport] = Type[EphemerisReport] (typeDefinition)
  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)
}
