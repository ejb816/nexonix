package draco

trait Holon[T <: Product] extends Extensible with Primal[T]

object Holon extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Holon", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Holon[_]] = Type[Holon[_]] (typeDefinition)
}
