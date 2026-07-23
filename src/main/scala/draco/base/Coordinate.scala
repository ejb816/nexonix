package draco.base

import draco._

trait Coordinate[T <: Product] extends Holon[T] {
  val value: T
}

object Coordinate extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Coordinate", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Coordinate[_]] = Type[Coordinate[_]] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
