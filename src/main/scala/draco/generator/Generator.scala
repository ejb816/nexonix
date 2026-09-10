package draco.generator

import draco._

trait Generator extends DracoType

object Generator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Generator", _namePackage = Seq ("draco", "generator")))
  lazy val dracoType: Type[Generator] = Type[Generator] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Emission", "EmissionReceived")

  lazy val domainType: Domain[Generator] = Domain[Generator] (typeDefinition)
}
