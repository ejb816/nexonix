package draco

trait Primal [T] extends Extensible {
  val value : T
}

object Primal extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Primal", _namePackage = Seq("draco")))
  lazy val dracoType: Type[Primal[_]] = Type[Primal[_]] (typeDefinition)
}