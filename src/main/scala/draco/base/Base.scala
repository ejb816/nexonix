package draco.base

import draco._

trait Base extends DracoType

object Base extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Base", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Base] = Type[Base] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Cardinal", "Coordinate", "Distance", "Meters", "Nominal", "Ordinal", "Radians", "Rotation", "Unit")

  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
