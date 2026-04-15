package draco

trait Primal [T] extends TypeInstance {
  val value : T
}

object Primal extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Primal", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Primal[_]] = Type[Primal[_]] (typeDefinition)
}