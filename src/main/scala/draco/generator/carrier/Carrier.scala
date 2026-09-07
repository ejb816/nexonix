package draco.generator.carrier

import draco.generator._
import draco._

trait Carrier extends DracoType

object Carrier extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Carrier", _namePackage = Seq ("draco", "generator", "carrier")))
  lazy val dracoType: Type[Carrier] = Type[Carrier] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("DefinitionPath")

  lazy val domainType: Domain[Carrier] = Domain[Carrier] (typeDefinition)
}
