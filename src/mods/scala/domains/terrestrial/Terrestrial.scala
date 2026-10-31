package domains.terrestrial

import draco._
import domains._
import domains.world._

trait Terrestrial extends World

object Terrestrial extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Terrestrial", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Terrestrial] = Type[Terrestrial] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("MarchIntent", "LocationReport", "ConsumeReport", "OriginateReport", "Consumer", "Creator", "Location", "Output")

  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)
}
