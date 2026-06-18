package domains.ethereal

import draco._
import domains.world.World

trait Ethereal extends World

object Ethereal extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Ethereal", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[Ethereal] = Type[Ethereal] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("LaunchIntent", "EphemerisReport")

  lazy val domainType: Domain[Ethereal] = Domain[Ethereal] (typeDefinition)
}
