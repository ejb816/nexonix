package draco.base

trait Coordinate[T <: Product] extends draco.Primal[T]

object Coordinate extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Coordinate[T <: Product]",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("Primal[T]", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      draco.Fixed ("value", "T")
    )
  )
  lazy val typeInstance: draco.Type[Coordinate[_]] = draco.Type[Coordinate[_]] (typeDefinition)

  def apply[T <: Product] (t: T): Coordinate[T] = new Coordinate[T] {
    override val value: T = t
    override val typeDefinition: draco.TypeDefinition = Coordinate.typeDefinition
  }
}