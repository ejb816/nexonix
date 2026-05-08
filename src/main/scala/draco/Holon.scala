package draco

trait Holon[T <: Product] extends Extensible with Primal[T]

object Holon extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Holon", _namePackage = Seq("draco")))
  lazy val dracoType: Type[Holon[_]] = Type[Holon[_]] (typeDefinition)
}
