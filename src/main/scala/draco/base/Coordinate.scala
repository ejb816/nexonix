package draco.base

trait Coordinate[T <: Product] extends draco.Holon[T]

object Coordinate extends App {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Coordinate", _namePackage = Seq("draco", "base")))
  lazy val dracoType: draco.Type[Coordinate[_]] = draco.Type[Coordinate[_]] (typeDefinition)

  def apply[T <: Product] (t: T): Coordinate[T] = new Coordinate[T] {
    override val value: T = t
  }
}