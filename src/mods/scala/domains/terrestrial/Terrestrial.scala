package domains.terrestrial

import draco._
import domains.world.World

trait Terrestrial extends World

object Terrestrial extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Terrestrial", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Terrestrial] = Type[Terrestrial] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("MarchIntent", "LocationReport", "ConsumeReport.rule", "OriginateReport.rule")

  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)
}
